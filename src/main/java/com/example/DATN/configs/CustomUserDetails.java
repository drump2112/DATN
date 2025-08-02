package com.example.DATN.configs;

import java.util.Collection;
import java.util.List;

import com.example.DATN.models.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

	private final User user;

	public CustomUserDetails(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		String roleName = user.getRole().getNameRole(); // VD: ADMIN
		return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	// Cho phép đăng nhập bằng username hoặc email
	@Override
	public String getUsername() {
		return user.getUserName(); // giữ nguyên, bạn sẽ xử lý ở service
	}

	@Override
	public boolean isEnabled() {
		return user.getIsActive();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	public User getUser() {
		return user;
	}

	public String getAvatar() {
		return user.getAvatar();
	}

}
