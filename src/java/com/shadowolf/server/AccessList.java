package com.shadowolf.server;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class that represents accesslist.xml. 
 * @see conf/accessList.xml
 */
@XmlRootElement(name="accessList")
public class AccessList {
	private List<Role> roles;
	private List<User> users;
	
	
	@XmlElement(name="role")
	@XmlElementWrapper(name="roles")
	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	@XmlElement(name="user")
	@XmlElementWrapper(name="users")
	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public static class Role {
		private String name;
		private List<String> urls;
		
		@XmlID
		@XmlAttribute
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		@XmlElement(name="url")
		public List<String> getUrls() {
			return urls;
		}
		public void setUrls(List<String> url) {
			this.urls = url;
		}
	}
	
	public static class User {
		private String username;
		private String password;
	
		private List<Role> roles;
		
		@XmlAttribute
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		@XmlAttribute
		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		@XmlIDREF
		@XmlElement(name="role")
		public List<Role> getRoles() {
			return roles;
		}

		public void setRoles(List<Role> roles) {
			this.roles = roles;
		}

		
	}
	
}