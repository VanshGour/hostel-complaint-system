package com.hostel.complaint.dto;

import com.hostel.complaint.model.Role;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private String hostelBlock;
    private String roomNumber;
    private String tradeSpecialty;

    public AuthResponse() {}

    public AuthResponse(String token, Long id, String email, String fullName, Role role, 
                        String hostelBlock, String roomNumber, String tradeSpecialty) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.hostelBlock = hostelBlock;
        this.roomNumber = roomNumber;
        this.tradeSpecialty = tradeSpecialty;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getHostelBlock() { return hostelBlock; }
    public void setHostelBlock(String hostelBlock) { this.hostelBlock = hostelBlock; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getTradeSpecialty() { return tradeSpecialty; }
    public void setTradeSpecialty(String tradeSpecialty) { this.tradeSpecialty = tradeSpecialty; }
}
