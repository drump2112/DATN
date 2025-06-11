// package com.example.DATN.configs;
//
// import java.util.Collection;
// import java.util.Collections;
//
// import java.util.List;
//
// import com.example.DATN.models.User;
//
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.UserDetails;
//
// public class CustomUserDetails implements UserDetails {
//
// private static final long serialVersionUID = 1L;
//
// private User user;
//
// public CustomUserDetails(User user) {
// super();
// this.user = user;
// }
//
// public User getUser() {
// return this.user;
// }
//
// @Override
// public Collection<? extends GrantedAuthority> getAuthorities() {
// if (user.getRole() != null) {
// return Collections.singletonList(
// new SimpleGrantedAuthority("ROLE_" + user.getRole().getNameRole()));
// }
// return Collections.emptyList();
//
// }
//
// @Override
// public String getPassword() {
// return user.getPassword();
// }
//
// @Override
// public String getUsername() {
// return user.getUserName();
// }
//
// @Override
// public boolean isAccountNonExpired() {
// return true; // tùy bạn có muốn dùng logic expire không
// }
//
// @Override
// public boolean isAccountNonLocked() {
// return true;
// }
//
// @Override
// public boolean isCredentialsNonExpired() {
// return true;
// }
//
// @Override
// public boolean isEnabled() {
// return true; // có thể thêm cờ "active" vào User nếu muốn quản lý
// }
//
// }
