package com.zhixindu.apply.core.apply.dao;

import com.zhixindu.apply.facade.apply.bo.ApplyBO;
import com.zhixindu.apply.facade.apply.bo.ApplyLoanDetailBO;
import com.zhixindu.apply.facade.apply.bo.ApplyMgtDetailBO;
import com.zhixindu.apply.facade.apply.bo.ApplyMgtPageParam;
import com.zhixindu.apply.facade.apply.bo.ApplyPageParam;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplyMapper {

    int insert(ApplyBO record);

    int insertSelective(ApplyBO record);

    ApplyBO selectByPrimaryKey(Integer apply_id);

    int updateByPrimaryKeySelective(ApplyBO record);

    int updateByPrimaryKey(ApplyBO record);

    ApplyBO selectByLenderId(Integer lender_id);

    List<ApplyMgtDetailBO> selectListForMgtByPage(ApplyMgtPageParam applyMgtPageParam);

    List<ApplyLoanDetailBO> selectListByPage(ApplyPageParam pageParam);

}