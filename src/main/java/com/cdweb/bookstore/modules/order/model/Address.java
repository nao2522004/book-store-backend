package com.cdweb.bookstore.modules.order.model;

import com.cdweb.bookstore.modules.user.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phone;

    private String street;
    private String ward;      // Phường/Xã
    private String district;  // Quận/Huyện
    private String province;  // Tỉnh/Thành phố

    @Column(name = "is_default")
    private boolean isDefault;
}