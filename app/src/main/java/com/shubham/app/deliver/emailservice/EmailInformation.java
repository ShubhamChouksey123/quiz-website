package com.shubham.app.deliver.emailservice;

import org.springframework.core.io.Resource;

import java.util.Map;

public class EmailInformation {

	/*- receiver information of the user */
	private String receiverPersonalName;
	private String receiverEmail;

	private String subject;

	/* parameters for Text Email */
	private String body;

	/* parameters for Html template Email */
	private Map<String, Object> parameterMap;
	private String templateName;
	private Map<String, Resource> parameterResourceMap;

	public EmailInformation() {
	}

	public EmailInformation(String receiverPersonalName, String receiverEmail, String subject, String body) {
		this.receiverPersonalName = receiverPersonalName;
		this.receiverEmail = receiverEmail;
		this.subject = subject;
		this.body = body;
	}

	public EmailInformation(String receiverPersonalName, String receiverEmail, String subject,
							Map<String, Object> parameterMap, String templateName, Map<String, Resource> parameterResourceMap) {
		this.receiverPersonalName = receiverPersonalName;
		this.receiverEmail = receiverEmail;
		this.subject = subject;
		this.parameterMap = parameterMap;
		this.templateName = templateName;
		this.parameterResourceMap = parameterResourceMap;
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

	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public Map<String, Resource> getParameterResourceMap() {
		return parameterResourceMap;
	}

	public void setParameterResourceMap(Map<String, Resource> parameterResourceMap) {
		this.parameterResourceMap = parameterResourceMap;
	}

	@Override
	public String toString() {
		return "EmailInformation{" + "receiverPersonalName='" + receiverPersonalName + '\'' + ", receiverEmail='"
				+ receiverEmail + '\'' + ", subject='" + subject + '\'' + ", body='" + body + '\'' + ", parameterMap="
				+ parameterMap + ", templateName='" + templateName + '\'' + ", parameterResourceMap="
				+ parameterResourceMap + '}';
	}
}
