package com.dabashou.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabashou.common.exception.BusinessException;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.user.domain.UserCampusAuth;
import com.dabashou.user.dto.CampusAuthDto;
import com.dabashou.user.mapper.UserCampusAuthMapper;
import com.dabashou.user.service.UserCampusAuthService;
import com.dabashou.user.vo.CampusAuthVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserCampusAuthServiceImpl extends ServiceImpl<UserCampusAuthMapper, UserCampusAuth>
        implements UserCampusAuthService {

    @Override
    @Transactional
    public void applyCampusAuth(Long userId, CampusAuthDto dto) {
        UserCampusAuth existing = lambdaQuery()
                .eq(UserCampusAuth::getUserId, userId)
                .one();
        if (existing != null && existing.getStatus() == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "已有待审核的认证申请");
        }
        if (existing != null && existing.getStatus() == 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "已通过认证");
        }
        UserCampusAuth auth = existing != null ? existing : new UserCampusAuth();
        auth.setUserId(userId);
        auth.setAuthType(dto.getAuthType());
        auth.setStudentNo(dto.getStudentNo());
        auth.setRealName(dto.getRealName());
        auth.setCampus(dto.getCampus());
        auth.setCollege(dto.getCollege());
        auth.setCredentialFileId(dto.getCredentialFileId());
        auth.setStatus(0);
        auth.setReviewRemark(null);
        auth.setReviewerId(null);
        auth.setReviewTime(null);
        if (existing != null) {
            updateById(auth);
        } else {
            save(auth);
        }
    }

    @Override
    public CampusAuthVo getCampusAuth(Long userId) {
        UserCampusAuth auth = lambdaQuery()
                .eq(UserCampusAuth::getUserId, userId)
                .one();
        if (auth == null) {
            return null;
        }
        return toVo(auth);
    }

    @Override
    @Transactional
    public void reviewCampusAuth(Long authId, Long reviewerId, boolean approved, String remark) {
        UserCampusAuth auth = getById(authId);
        if (auth == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "认证记录不存在");
        }
        if (auth.getStatus() != 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该认证已处理");
        }
        auth.setStatus(approved ? 1 : 2);
        auth.setReviewerId(reviewerId);
        auth.setReviewRemark(remark);
        auth.setReviewTime(LocalDateTime.now());
        updateById(auth);
    }

    private CampusAuthVo toVo(UserCampusAuth auth) {
        CampusAuthVo vo = new CampusAuthVo();
        vo.setId(auth.getId());
        vo.setAuthType(auth.getAuthType());
        vo.setStudentNo(auth.getStudentNo());
        vo.setRealName(auth.getRealName());
        vo.setCampus(auth.getCampus());
        vo.setCollege(auth.getCollege());
        vo.setCredentialFileId(auth.getCredentialFileId());
        vo.setStatus(auth.getStatus());
        vo.setStatusName(auth.getStatus() == 0 ? "待审核" : auth.getStatus() == 1 ? "已通过" : "已拒绝");
        vo.setReviewRemark(auth.getReviewRemark());
        vo.setCreateTime(auth.getCreateTime());
        return vo;
    }
}
