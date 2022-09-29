package com.shubham.app.deliver.emailservice;

public class EmailInformation {

	/*- receiver information of the user */
	private String receiverPersonalName;
	private String receiverEmail;

	private String subject;
	private String body;

	public EmailInformation() {
	}

	public EmailInformation(String receiverPersonalName, String receiverEmail, String subject, String body) {
		this.receiverPersonalName = receiverPersonalName;
		this.receiverEmail = receiverEmail;
		this.subject = subject;
		this.body = body;
	}

	public String getReceiverPersonalName() {
		return receiverPersonalName;
	}

	public String getReceiverEmail() {
		return receiverEmail;
	}

	public String getSubject() {
		return subject;
	}

	public String getBody() {
		return body;
	}

	public void setReceiverPersonalName(String receiverPersonalName) {
		this.receiverPersonalName = receiverPersonalName;
	}

	public void setReceiverEmail(String receiverEmail) {
		this.receiverEmail = receiverEmail;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "EmailInformation{" + "receiverPersonalName='" + receiverPersonalName + '\'' + ", receiverEmail='"
				+ receiverEmail + '\'' + ", subject='" + subject + '\'' + ", body='" + body + '\'' + '}';
	}
}
