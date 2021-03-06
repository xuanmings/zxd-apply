package com.zhixindu.apply.core.apply.business;

import com.google.common.collect.Lists;
import com.zhixindu.apply.core.apply.dao.ApplyAddressMapper;
import com.zhixindu.apply.core.apply.dao.ApplyBankCardMapper;
import com.zhixindu.apply.core.apply.dao.ApplyContactMapper;
import com.zhixindu.apply.core.apply.dao.ApplyLocationMapper;
import com.zhixindu.apply.core.apply.dao.ApplyMapper;
import com.zhixindu.apply.core.apply.dao.ApplyStepMapper;
import com.zhixindu.apply.core.apply.po.ApplyContactPO;
import com.zhixindu.apply.core.apply.po.ApplyPO;
import com.zhixindu.apply.core.apply.po.ApplyStepPO;
import com.zhixindu.apply.core.apply.service.ApplyService;
import com.zhixindu.apply.core.constant.ApplyErrorCode;
import com.zhixindu.apply.facade.applicant.enums.WorkState;
import com.zhixindu.apply.facade.apply.bo.ApplyAddressBO;
import com.zhixindu.apply.facade.apply.bo.ApplyBO;
import com.zhixindu.apply.facade.apply.bo.ApplyBankCardBO;
import com.zhixindu.apply.facade.apply.bo.ApplyBaseInfoBO;
import com.zhixindu.apply.facade.apply.bo.ApplyContactBO;
import com.zhixindu.apply.facade.apply.bo.ApplyCreditBO;
import com.zhixindu.apply.facade.apply.bo.ApplyLoanBO;
import com.zhixindu.apply.facade.apply.bo.ApplyLoanDetailBO;
import com.zhixindu.apply.facade.apply.bo.ApplyLoanInfoBO;
import com.zhixindu.apply.facade.apply.bo.ApplyLoanStepBO;
import com.zhixindu.apply.facade.apply.bo.ApplyLocationBO;
import com.zhixindu.apply.facade.apply.bo.ApplyPageParam;
import com.zhixindu.apply.facade.apply.bo.ApplyStatusBO;
import com.zhixindu.apply.facade.apply.business.DubboApplyWechatBusiness;
import com.zhixindu.apply.facade.apply.enums.ApplyStatus;
import com.zhixindu.apply.facade.apply.enums.ProcessState;
import com.zhixindu.apply.facade.apply.enums.ProcessStep;
import com.zhixindu.commons.annotation.Business;
import com.zhixindu.commons.api.ServiceCode;
import com.zhixindu.commons.api.ServiceException;
import com.zhixindu.commons.page.PageResult;
import com.zhixindu.commons.repository.PageRepository;
import com.zhixindu.commons.utils.Parameters;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by SteveGuo on 2017/3/3.
 */
@Business("applyWechatBusiness")
public class ApplyWechatBusinessImpl implements DubboApplyWechatBusiness {

    @Inject
    private ApplyService applyService;
    @Inject
    private ApplyMapper applyMapper;
    @Inject
    private ApplyStepMapper applyStepMapper;
    @Inject
    private ApplyLocationMapper applyLocationMapper;
    @Inject
    private ApplyAddressMapper applyAddressMapper;
    @Inject
    private ApplyContactMapper applyContactMapper;
    @Inject
    private ApplyBankCardMapper applyBankCardMapper;
    @Inject
    private PageRepository pageRepository;

    @Override
    public boolean isBeforeAMonthFromLastApply(Integer applicantId) {
        Parameters.requireNotNull(applicantId, "applicantId不能为空");
        Date lastApplyTime = applyMapper.selectLastApplyTime(applicantId);
        return null != lastApplyTime && DateTime.now().minusMonths(1).isBefore(lastApplyTime.getTime());
    }

    @Override
    public boolean hasNotSettledApply(Integer applicantId) {
        Parameters.requireNotNull(applicantId, "applicantId不能为空");
        return applyMapper.countNotSettledApply(applicantId) > 0;
    }

    @Override
    public boolean hasSettledApply(Integer applicantId) throws ServiceException {
        ApplyPO applyPO = applyMapper.selectLatestByApplicantId(applicantId);
        return null != applyPO && ApplyStatus.REPAYMENT_SETTLED.matches(applyPO.getApply_status());
    }

    @Override
    public ApplyBaseInfoBO findLatestReviewApply(Integer applicantId) {
        Parameters.requireNotNull(applicantId, "applicantId不能为空");
        return applyMapper.selectLatestReviewByApplicantId(applicantId);
    }

    @Override
    public Integer findPrepareApplyId(Integer applicantId) {
        return applyMapper.selectPreparePrimaryKeyByApplicantId(applicantId);
    }

    @Override
    public ApplyLoanInfoBO startApplyLoan(Integer applicantId, boolean isQueryAddress) {
        Parameters.requireNotNull(applicantId, "applicantId不能为空");
        Integer applyId = applyMapper.selectPreparePrimaryKeyByApplicantId(applicantId);
        if(null == applyId) {
            applyId = applyService.startApplyLoan(applicantId);
        }
        ApplyLoanInfoBO applyLoanInfoBO = new ApplyLoanInfoBO();
        applyLoanInfoBO.setApply_id(applyId);
        if(isQueryAddress) {
            ApplyAddressBO applyAddressBO = applyAddressMapper.selectLatestByApplicantId(applicantId);
            if(null != applyAddressBO) {
                applyLoanInfoBO.setApplyAddressBO(applyAddressBO);
            }
        }
        return applyLoanInfoBO;
    }

    @Override
    public ApplyAddressBO findLatestApplyAddress(Integer applicantId) {
        Parameters.requireNotNull(applicantId, "applicantId不能为空");
        return applyAddressMapper.selectLatestByApplicantId(applicantId);
    }

    @Override
    public List<ApplyContactBO> findLatestApplyContact(Integer applicantId) {
        Parameters.requireNotNull(applicantId, "applicantId不能为空");
        List<ApplyContactPO> applyContactPOList = applyContactMapper.selectLatestByApplicantId(applicantId);
        if(CollectionUtils.isEmpty(applyContactPOList)){
            return Lists.newArrayListWithCapacity(0);
        }
        return applyContactPOList.stream()
                .map(applyContactPO -> {
                    ApplyContactBO applyContactBO = new ApplyContactBO();
                    BeanUtils.copyProperties(applyContactPO, applyContactBO);
                    return applyContactBO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ApplyBankCardBO findLatestApplyBankCard(Integer applicantId) {
        Parameters.requireNotNull(applicantId, "applicantId不能为空");
        return applyBankCardMapper.selectLatestByApplicantId(applicantId);
    }

    @Override
    public ApplyAddressBO findApplyAddress(Integer applyId) {
        Parameters.requireNotNull(applyId, "applyId不能为空");
        return applyAddressMapper.selectByApplyId(applyId);
    }

    @Override
    public List<ApplyContactBO> findApplyContact(Integer applyId) {
        Parameters.requireNotNull(applyId, "applyId不能为空");
        List<ApplyContactPO> applyContactPOList = applyContactMapper.selectByApplyId(applyId);
        if(CollectionUtils.isEmpty(applyContactPOList)) {
            return Lists.newArrayListWithCapacity(0);
        }
        return applyContactPOList.stream()
                .map(applyContactPO -> {
                    ApplyContactBO applyContactBO = new ApplyContactBO();
                    BeanUtils.copyProperties(applyContactPO, applyContactBO);
                    return applyContactBO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ApplyBankCardBO findApplyBankCard(Integer applyId) {
        return applyBankCardMapper.selectByApplyId(applyId);
    }

    @Override
    public Integer submitApplyAddress(ApplyAddressBO applyAddressBO) {
        Integer applyId = applyAddressBO.getApply_id();
        Parameters.requireNotNull(applyId, "applyId不能为空");
        List<String> ignoreProperties = new ArrayList<>();
        if(applyService.existApplyAddress(applyId)) {
            applyAddressBO.setAddress_id(applyAddressMapper.selectPrimaryKeyByApplyId(applyId));
            if(!WorkState.EMPLOYEE.matches(applyAddressBO.getWork_state())) {
                ignoreProperties.add("company_name");
                ignoreProperties.add("company_address_code");
                ignoreProperties.add("company_address");
            }
        } else {
            ignoreProperties.add("address_id");
            if (!WorkState.EMPLOYEE.matches(applyAddressBO.getWork_state())) {
                ignoreProperties.add("company_name");
                ignoreProperties.add("company_address_code");
                ignoreProperties.add("company_address");
            }
        }
        Parameters.requireAllPropertyNotNull(applyAddressBO, ignoreProperties);
        return applyService.saveOrUpdateAddress(applyAddressBO);
    }

    @Override
    public List<Integer> submitApplyContact(List<ApplyContactBO> applyContactBOList) {
        if(CollectionUtils.isEmpty(applyContactBOList)) {
            throw new ServiceException(ServiceCode.ILLEGAL_PARAM, "联系人参数不能为空");
        }
        applyContactBOList = applyContactBOList.stream()
                .map(applyContactBO -> {
            Integer applyId = applyContactBO.getApply_id();
            Parameters.requireNotNull(applyId, "applyId不能为空");
            List<String> ignoreProperties = new ArrayList<>();
            if (!applyService.existApplyContact(applyId)) {
                ignoreProperties.add("contact_id");
            }
            Parameters.requireAllPropertyNotNull(applyContactBO, ignoreProperties);
            return applyContactBO;
        }).collect(Collectors.toList());
        return applyService.saveOrUpdateContact(applyContactBOList);
    }

    @Override
    public Integer submitApplyBankCard(ApplyBankCardBO applyBankCardBO) {
        Integer applyId = applyBankCardBO.getApply_id();
        Parameters.requireNotNull(applyId, "applyId不能为空");
        List<String> ignoreProperties = new ArrayList<>();
        if(applyService.existApplyBankCard(applyId)) {
            applyBankCardBO.setBank_card_id(applyBankCardMapper.selectPrimaryKeyByApplyId(applyId));
        }else {
            ignoreProperties.add("bank_card_id");
        }
        Parameters.requireAllPropertyNotNull(applyBankCardBO, ignoreProperties);
        return applyService.saveOrUpdateBankCard(applyBankCardBO);
    }

    @Override
    public boolean completeCertification(Integer applicantId) {
        Parameters.requireNotNull(applicantId, "applicantId不能为空");
        return applyService.completeCertification(applicantId);
    }

    @Override
    public boolean prepareApplyLoan(Integer applicantId, Integer applyId) throws ServiceException {
        return applyService.prepareApplyLoan(applicantId, applyId);
    }

    @Override
    public String findBankCardNumber(Integer applyId) {
        Parameters.requireNotNull(applyId, "applyId不能为空");
        return applyBankCardMapper.selectBankCardNumber(applyId);
    }

    @Override
    public Integer findAddressId(Integer applyId) {
        Parameters.requireNotNull(applyId, "applyId不能为空");
        return applyAddressMapper.selectPrimaryKeyByApplyId(applyId);
    }

    @Override
    public List<Integer> findContactIdList(Integer applyId) {
        Parameters.requireNotNull(applyId, "applyId不能为空");
        return applyContactMapper.selectPrimaryKeyByApplyId(applyId);
    }

    @Override
    public Integer findBankCardId(Integer applyId) {
        Parameters.requireNotNull(applyId, "applyId不能为空");
        return applyBankCardMapper.selectPrimaryKeyByApplyId(applyId);
    }

    @Override
    public Integer submitApplyLoan(ApplyBaseInfoBO applyBaseInfoBO) {
        Parameters.requireAllPropertyNotNull(applyBaseInfoBO);
        Integer applicantId = applyBaseInfoBO.getApplicant_id();
        if(hasNotSettledApply(applicantId)) {
            throw new ServiceException(ApplyErrorCode.HAS_NOT_SETTLED_APPLY.getErrorCode(), ApplyErrorCode
                    .HAS_NOT_SETTLED_APPLY.getDesc());
        }
        Integer applyId = applyBaseInfoBO.getApply_id();
        checkApplyLoan(applyId);
        return applyService.saveApplyLoan(applyBaseInfoBO);
    }

    @Override
    public PageResult<ApplyLoanBO> findApplyLoanList(ApplyPageParam pageParam) {
        Parameters.requireAllPropertyNotNull(pageParam, "分页查询参数不能为空");
        PageResult<ApplyPO> applyBOPageResult = pageRepository.selectPaging(ApplyMapper.class, "selectListByPage", pageParam);
        PageResult<ApplyLoanBO> pageResult = new PageResult<>();
        BeanUtils.copyProperties(applyBOPageResult, pageResult);

        List<ApplyPO> applyPOList = applyBOPageResult.getRows();
        List<ApplyLoanBO> applyLoanBOList = Lists.newArrayList();
        if(CollectionUtils.isNotEmpty(applyPOList)){
            applyLoanBOList = applyPOList.stream().map(applyBO -> {
                ApplyLoanBO applyLoanBO = new ApplyLoanBO();
                BeanUtils.copyProperties(applyBO, applyLoanBO);
                applyLoanBO.setApply_time(new DateTime(applyBO.getApply_time()).toString("yyyy-MM-dd"));
                applyLoanBO.setApply_status(applyBO.getApply_status().getDesc());
                applyLoanBO.setApply_status_value(applyBO.getApply_status().getValue());
                return applyLoanBO;
            }).collect(Collectors.toList());
        }
        pageResult.setRows(applyLoanBOList);
        return pageResult;
    }

    @Override
    public ApplyLoanDetailBO findApplyLoanDetail(Integer applyId) {
        Parameters.requireNotNull(applyId, "applyId不能为空");
        ApplyLoanDetailBO applyLoanDetailBO = new ApplyLoanDetailBO();
        ApplyPO applyPO = applyMapper.selectByPrimaryKey(applyId);
        if(null == applyPO) {
            throw new ServiceException(ApplyErrorCode.NO_APPLY.getErrorCode(), ApplyErrorCode.NO_APPLY.getDesc());
        }
        BeanUtils.copyProperties(applyPO, applyLoanDetailBO);
        applyLoanDetailBO.setApply_time(new DateTime(applyPO.getApply_time()).toString("yyyy-MM-dd HH:mm:ss"));
        applyLoanDetailBO.setApply_status(applyPO.getApply_status().getDesc());
        applyLoanDetailBO.setApply_status_value(applyPO.getApply_status().getValue());

        List<ApplyStepPO> applyStepPOList = applyStepMapper.selectListByApplyId(applyId);
        List<ApplyLoanStepBO> applyLoanStepBOList = Lists.newArrayListWithCapacity(0);
        if(CollectionUtils.isNotEmpty(applyStepPOList)) {
            applyLoanStepBOList = applyStepPOList.stream().map(instanceBO -> {
                ApplyLoanStepBO applyLoanStepBO = new ApplyLoanStepBO();
                ProcessStep processStep = instanceBO.getProcess_step();
                ProcessState processState = instanceBO.getProcess_state();
                ApplyStatus applyStatus = ApplyStatus.resolve(processStep, processState);
                applyLoanStepBO.setProcess_result(applyStatus.getDesc());
                applyLoanStepBO.setProcess_result_value(applyStatus.getValue());
                if(ProcessState.PROCESSING.matches(processState)) {
                    applyLoanStepBO.setProcess_time(processStep.getProcessDesc());
                } else {
                    applyLoanStepBO.setProcess_time(new DateTime(instanceBO.getProcess_time()).toString("yyyy-MM-dd HH:mm:ss"));
                }
                return applyLoanStepBO;
            }).collect(Collectors.toList());
        }
        applyLoanDetailBO.setApplyLoanStepBOList(applyLoanStepBOList);
        return applyLoanDetailBO;
    }

    @Override
    public ApplyBO findApply(Integer applyId) {
        Parameters.requireNotNull(applyId, "applyId不能为空");
        ApplyPO applyPO = applyMapper.selectByPrimaryKey(applyId);
        if(null == applyPO) {
            throw new ServiceException(ApplyErrorCode.NO_APPLY.getErrorCode(), ApplyErrorCode.NO_APPLY.getDesc());
        }
        ApplyLocationBO applyLocationBO = applyLocationMapper.selectByApplyId(applyPO.getApply_id());
        applyPO.setLatitude(applyLocationBO.getLatitude());
        applyPO.setLongitude(applyLocationBO.getLongitude());
        applyPO.setPrecision(applyLocationBO.getPrecision());
        ApplyBO applyBO = new ApplyBO();
        BeanUtils.copyProperties(applyPO, applyBO);
        return applyBO;
    }

    @Override
    public boolean submitApplyCredit(ApplyCreditBO applyCreditBO) {
        List<String> ignoreProperties = new ArrayList<>();
        if(ApplyStatus.REVIEW_FAIL.matches(applyCreditBO.getApply_status())) {
            ignoreProperties.add("credit_score");
        }
        Parameters.requireAllPropertyNotNull(applyCreditBO, ignoreProperties);
        return applyService.updateApplyCredit(applyCreditBO) > 0;
    }

    @Override
    public boolean submitLoanStatus(ApplyStatusBO applyStatusBO) {
        Parameters.requireAllPropertyNotNull(applyStatusBO);
        return applyService.updateLoanStatus(applyStatusBO) > 0;
    }

    @Override
    public boolean submitRepaymentStatus(ApplyStatusBO applyStatusBO) {
        Parameters.requireAllPropertyNotNull(applyStatusBO, Arrays.asList("apply_status"));
        return applyService.updateRepaymentStatus(applyStatusBO) > 0;
    }

    @Override
    public Integer findLatestApplyId(String customerId) throws ServiceException {
        Parameters.requireNotNull(customerId, "customerId不能为空");
        return applyMapper.selectLatestPrimaryKeyByCustomerId(customerId);
    }

    @Override
    public boolean checkApplyLoan(Integer applyId) throws ServiceException {
        if(!applyService.existApplyAddress(applyId)){
            throw new ServiceException(ApplyErrorCode.NO_APPLY_ADDRESS.getErrorCode(), ApplyErrorCode.NO_APPLY_ADDRESS.getDesc());
        }
        if(!applyService.existApplyContact(applyId)){
            throw new ServiceException(ApplyErrorCode.NO_APPLY_CONTACT.getErrorCode(), ApplyErrorCode.NO_APPLY_CONTACT.getDesc());
        }
        return true;
    }
}
