# PostgreSQL Database Access Guide

**Last Updated**: October 2, 2025

This guide explains how to connect to the PostgreSQL database running in a Docker container on the OCI instance using PgAdmin or other database tools.

---

## Overview

The quiz application uses PostgreSQL 14 running in a Docker container (`quiz-postgres`) on the OCI compute instance. For security, PostgreSQL port 5432 is **not** exposed publicly - it only listens within the Docker network.

To access the database remotely, you must use an **SSH tunnel** to securely forward the connection through the OCI instance.

---

## Database Configuration

| Property | Value |
|----------|-------|
| **Database Name** | `quiz` |
| **Username** | `postgres` |
| **Password** | Check `.env` file: `DB_ADMIN_PASSWORD` |
| **Container Name** | `quiz-postgres` |
| **Container Port** | `5432` (internal only) |
| **Container IP** | `172.20.0.2` (Docker bridge network) |
| **Docker Network** | `quiz-app_quiz-network` |

---

## Prerequisites

1. **SSH Access**: You must have SSH access to the OCI instance
   - SSH Key: `~/.ssh/id_rsa`
   - User: `opc`
   - Host: `161.118.188.237`

2. **PgAdmin Installed**: Download from [pgadmin.org](https://www.pgadmin.org/download/)

3. **PostgreSQL Client Tools** (optional, for command-line access):
   ```bash
   brew install postgresql  # macOS
   ```

---

## Method 1: PgAdmin with External SSH Tunnel (Recommended)

This method uses a separate SSH tunnel that you manage independently of PgAdmin.

### Step 1: Create SSH Tunnel

Open a terminal and run:

```bash
ssh -f -i ~/.ssh/id_rsa -o ServerAliveInterval=60 -L 5433:172.20.0.2:5432 opc@161.118.188.237 -N
```

**What this does:**
- `-f`: Runs in background
- `-i ~/.ssh/id_rsa`: Uses your SSH private key
- `-o ServerAliveInterval=60`: Keeps connection alive
- `-L 5433:172.20.0.2:5432`: Forwards local port 5433 to container IP 172.20.0.2 port 5432
- `opc@161.118.188.237`: SSH connection to OCI instance
- `-N`: Don't execute remote commands, just tunnel

**Keep this tunnel running** while using PgAdmin.

### Step 2: Configure PgAdmin

1. Open PgAdmin
2. Right-click **"Servers"** → **Register** → **Server**

#### General Tab:
- **Name**: `OCI Quiz Database` (or any name you prefer)

#### Connection Tab:
- **Host name/address**: `127.0.0.1`
- **Port**: `5433`
- **Maintenance database**: `quiz`
- **Username**: `postgres`
- **Password**: (Get from `.env` file - `DB_ADMIN_PASSWORD`)
- ✅ **Save password**: ON

#### SSH Tunnel Tab:
- ❌ **Use SSH tunneling**: OFF (we're using external tunnel)

3. Click **"Save"**
4. Connect to the server

### Step 3: Verify Connection

Once connected, you should see:
- Database: `quiz`
- Tables: `question`, `quiz_leaderboard`, `quiz_result`, etc.
- Current question count: Check with `SELECT COUNT(*) FROM question;`

### Step 4: Managing the SSH Tunnel

**Check if tunnel is running:**
```bash
lsof -i :5433
```

**Stop the tunnel:**
```bash
pkill -f "ssh.*5433"
```

**Restart the tunnel** (if container IP changes after restart):
```bash
# Get new container IP
ssh -i ~/.ssh/id_rsa opc@161.118.188.237 \
  "docker inspect quiz-postgres | jq -r '.[0].NetworkSettings.Networks | to_entries[] | .value.IPAddress'"

# Create tunnel with new IP (replace 172.20.0.2 if needed)
ssh -f -i ~/.ssh/id_rsa -o ServerAliveInterval=60 -L 5433:172.20.0.2:5432 opc@161.118.188.237 -N
```

---

## Troubleshooting

### Issue 1: SSH Tunnel Connection Refused

**Symptoms:**
```
connection to server at "127.0.0.1", port 5433 failed: Connection refused
```

**Solution:**
```bash
# Check if tunnel is running
lsof -i :5433

# If not running, create it
ssh -f -i ~/.ssh/id_rsa -o ServerAliveInterval=60 -L 5433:172.20.0.2:5432 opc@161.118.188.237 -N
```

### Issue 2: Password Authentication Failed

**Symptoms:**
```
FATAL: password authentication failed for user "postgres"
```

**Solutions:**

1. **Verify you're using the correct password from `.env` file**

2. **Check if password was changed on server:**
   ```bash
   ssh -i ~/.ssh/id_rsa opc@161.118.188.237 "grep DB_ADMIN_PASSWORD /opt/quiz-app/.env"
   ```

3. **Reset password if needed:**
   ```bash
   ssh -i ~/.ssh/id_rsa opc@161.118.188.237 \
     "docker exec quiz-postgres psql -U postgres -d quiz -c \"ALTER USER postgres WITH PASSWORD '<YOUR_PASSWORD>';\""
   ```

### Issue 3: PostgreSQL Permission Denied Errors

**Symptoms:**
```
FATAL: could not open file "global/pg_filenode.map": Permission denied
```

**Solution:**
```bash
ssh -i ~/.ssh/id_rsa opc@161.118.188.237 \
  "cd /opt/quiz-app && docker compose down && \
   sudo chown -R 70:70 /opt/quiz-app/data/postgres && \
   sudo chmod -R 750 /opt/quiz-app/data/postgres && \
   docker compose up -d"
```

Wait 1-2 minutes for PostgreSQL to start, then recreate SSH tunnel.

### Issue 4: Container IP Changed

**Symptoms:** Tunnel connects but database queries fail

**Solution:**
```bash
# Get current container IP
ssh -i ~/.ssh/id_rsa opc@161.118.188.237 \
  "docker inspect quiz-postgres --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'"

# Kill old tunnel
pkill -f "ssh.*5433"

# Create new tunnel with updated IP
ssh -f -i ~/.ssh/id_rsa -o ServerAliveInterval=60 -L 5433:<NEW_IP>:5432 opc@161.118.188.237 -N
```

### Issue 5: PgAdmin Special Character Password Issues

If you encounter password issues with PgAdmin specifically (but command-line works), the password might contain special characters that PgAdmin doesn't handle well.

**Solutions:**
- Use the external SSH tunnel method (Method 1) - most reliable
- Try copy-pasting the password instead of typing it
- As a last resort, set a simpler password without special characters

---

**Document maintained by**: Shubham Chouksey
**Last tested**: October 2, 2025
