package com.example.onionstore.domain.cart.repository;

import com.example.onionstore.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart.user.id = :userId")
    List<CartItem> findByCart_UserId(@Param("userId") Long userId);

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.id IN :ids AND ci.cart.user.id = :userId")
    List<CartItem> findByIdInAndCart_User_IdWithProduct(@Param("ids") List<Long> cartItemIds, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.id IN :ids AND ci.cart.user.id = :userId")
    int deleteAllByIdInAndCart_User_Id(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    List<CartItem> findAllByCartIdOrderByIdAsc(Long cartId);


}
