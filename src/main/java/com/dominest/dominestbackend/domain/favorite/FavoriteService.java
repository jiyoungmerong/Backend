package com.dominest.dominestbackend.domain.favorite;

import com.dominest.dominestbackend.domain.post.component.category.Category;
import com.dominest.dominestbackend.domain.post.component.category.service.CategoryService;
import com.dominest.dominestbackend.domain.user.User;
import com.dominest.dominestbackend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    @Transactional
    // 즐겨찾기 추가 / 취소
    public boolean addOrUndoFavorite(Long categoryId, String userEmail) {
        // 일단 조인해. 없으면 예외처리해.
        Favorite favorite = favoriteRepository.findByCategoryIdAndUserEmail(categoryId, userEmail);
        if (favorite == null) {
            User user = userService.getUserByEmail(userEmail);
            Category category = categoryService.getCategoryById(categoryId);
            Favorite newFavorite = Favorite.builder()
                    .user(user)
                    .category(category)
                    .build();
            favoriteRepository.save(newFavorite);
            return newFavorite.isOnOff();
        }

        return favorite.switchOnOff();
    }
}












