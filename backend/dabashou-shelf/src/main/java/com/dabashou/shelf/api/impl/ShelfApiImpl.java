package com.dabashou.shelf.api.impl;

import com.dabashou.shelf.api.ShelfApi;
import com.dabashou.shelf.domain.SkillShelf;
import com.dabashou.shelf.service.SkillShelfService;
import org.springframework.stereotype.Service;

@Service
public class ShelfApiImpl implements ShelfApi {

    private final SkillShelfService shelfService;

    public ShelfApiImpl(SkillShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @Override
    public Integer getPointPrice(Long shelfId) {
        SkillShelf shelf = shelfService.getById(shelfId);
        return shelf != null ? shelf.getPointPrice() : null;
    }

    @Override
    public Long getSkillTagId(Long shelfId) {
        SkillShelf shelf = shelfService.getById(shelfId);
        return shelf != null ? shelf.getSkillTagId() : null;
    }

    @Override
    public Long getUserId(Long shelfId) {
        SkillShelf shelf = shelfService.getById(shelfId);
        return shelf != null ? shelf.getUserId() : null;
    }

    @Override
    public String getTitle(Long shelfId) {
        SkillShelf shelf = shelfService.getById(shelfId);
        return shelf != null ? shelf.getTitle() : null;
    }

    @Override
    public boolean isOnShelf(Long shelfId) {
        SkillShelf shelf = shelfService.getById(shelfId);
        return shelf != null && shelf.getStatus() == 1;
    }
}
