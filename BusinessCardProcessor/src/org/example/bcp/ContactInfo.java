package org.example.bcp;

import java.text.MessageFormat;

/**
 * Storage object for a person's b-card data.
 * @author astein
 *
 */
public class ContactInfo {
	
	private String name;
	
	private String phoneNumber;
	
	private String emailAddress;
	
	public ContactInfo(String name, String phoneNumber, String emailAddress) {
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
	}
	
	/**
	 * returns the full name of the individual (eg. John Smith, Susan Malick)
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * returns the phone number formatted as a sequence of digits
	 * @return
	 */
	public String getPhoneNumber() {
		return this.phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * returns the email address
	 * @return
	 */
	public String getEmailAddress() {
		return this.emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public String prettyPrint() {
		String format = "Name: {0}\n\nPhone: {1}\n\nEmail: {2}";
		return MessageFormat.format(format, name, phoneNumber, emailAddress);
	}
}
