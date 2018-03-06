package com.lnet.tms.service.dispatch;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lnet.framework.core.ListResponse;
import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;
import com.lnet.framework.core.ResponseBuilder;
import com.lnet.framework.util.ObjectQuery;
import com.lnet.framework.util.ReflectUtils;
import com.lnet.framework.util.Snowflake;
import com.lnet.model.tms.dispatch.dispatchDto.DispatchFeeApplyItemDto;
import com.lnet.model.tms.dispatch.dispatchEntity.*;
import com.lnet.model.ums.user.User;
import com.lnet.tms.contract.spi.dispatch.DispatchFeeApplyService;
import com.lnet.tms.mapper.*;
import com.lnet.ums.contract.api.ExpenseAccountService;
import com.lnet.ums.contract.api.UserService;
import com.lnet.model.ums.expense.ExpenseAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
@Slf4j
public class DispatchFeeApplyServiceImpl implements DispatchFeeApplyService {

    private final String className = this.getClass().getSimpleName() + ".";

    @Resource
    DispatchFeeApplyMapper feeApplyMapper;
    @Resource
    DispatchFeeApplyItemMapper feeApplyItemMapper;
    @Resource
    DispatchMapper dispatchMapper;
    @Resource
    DispatchFeeApplyPicMapper feeApplyPicMapper;
    @Resource
    DispatchFeeApplyOrderMapper feeApplyOrderMapper;

    @Resource
    ExpenseAccountService expenseAccountService;

    @Resource
    UserService userService;

    @Override
    public PageResponse<DispatchFeeApply> pageList(Integer page, Integer pageSize, Map<String, Object> params) {
        try {
            Assert.notNull(page);
            Assert.notNull(pageSize);
            PageHelper.startPage(page, pageSize);
            List<DispatchFeeApply> list = feeApplyMapper.pageList(params);
            PageInfo pageInfo = new PageInfo<>(list);
            return ResponseBuilder.page(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
        } catch (Exception e) {
            log.error(className + "pageList", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.pageFail(e);
        }
    }

    @Override
    public Response<DispatchFeeApply> get(String feeApplyId) {
        try {
            Assert.notNull(feeApplyId);
            DispatchFeeApply dispatchFeeApply = feeApplyMapper.get(feeApplyId);
            if (dispatchFeeApply == null) {
                return ResponseBuilder.fail("派车单申报不存在！");
            }
            List<DispatchFeeApplyItem> items = feeApplyItemMapper.findByApplyId(feeApplyId);
            dispatchFeeApply.setItems(items);
            List<DispatchFeeApplyPic> pics = feeApplyPicMapper.findByFeeApplyId(feeApplyId);
            dispatchFeeApply.setPics(pics);
            List<DispatchFeeApplyOrder> orders = feeApplyOrderMapper.findByFeeApplyId(feeApplyId);
            dispatchFeeApply.setOrders(orders);
            return ResponseBuilder.success(dispatchFeeApply);
        } catch (Exception e) {
            log.error(className + "get", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<DispatchFeeApply> create(DispatchFeeApply dispatchFeeApply) {
        try {
            Assert.notNull(dispatchFeeApply);
            Assert.notEmpty(dispatchFeeApply.getItems());
            Assert.notNull(dispatchFeeApply.getDispatchNumber());
            Dispatch dispatch = dispatchMapper.getByNo(dispatchFeeApply.getDispatchNumber());
            if (dispatch == null) {
                return ResponseBuilder.fail("派车单不存在！");
            }
            if (Dispatch.statusEnum.CANCELED.equals(dispatch.getStatus())) {
                return ResponseBuilder.fail("派车单已取消，不能申报费用！");
            }
            dispatchFeeApply.setApprove(false);
            dispatchFeeApply.setApplyTime(LocalDateTime.now());
            dispatchFeeApply.setFeeApplyId(Snowflake.getInstance().next());
            dispatchFeeApply.getItems().stream().forEach(feeApplyItem -> {
                feeApplyItem.setFeeApplyId(dispatchFeeApply.getFeeApplyId());
                feeApplyItem.setFeeApplyItemId(Snowflake.getInstance().next());
            });

            Assert.isTrue(feeApplyMapper.insert(dispatchFeeApply) == 1);
            Assert.isTrue(feeApplyItemMapper.batchInsert(dispatchFeeApply.getItems()) == dispatchFeeApply.getItems().size());

            List<DispatchFeeApplyPic> pics = dispatchFeeApply.getPics();
            if (pics != null && pics.size() > 0) {
                pics.stream().forEach(pic -> {
                    pic.setFeeApplyPicId(Snowflake.getInstance().next());
                    pic.setFeeApplyId(dispatchFeeApply.getFeeApplyId());
                });
                Assert.isTrue(feeApplyPicMapper.batchInsert(pics) == pics.size());
            }
            List<DispatchFeeApplyOrder> dispatchFeeApplyOrders = dispatchFeeApply.getOrders();
            if (dispatchFeeApplyOrders != null && dispatchFeeApplyOrders.size() > 0) {
                dispatchFeeApplyOrders.stream().forEach(dispatchFeeApplyOrder -> {
                    dispatchFeeApplyOrder.setFeeApplyOrderId(Snowflake.getInstance().next());
                    dispatchFeeApplyOrder.setFeeApplyId(dispatchFeeApply.getFeeApplyId());
                });
                Assert.isTrue(feeApplyOrderMapper.batchInsert(dispatchFeeApplyOrders) == dispatchFeeApplyOrders.size());
            }

            return ResponseBuilder.success(dispatchFeeApply);
        } catch (Exception e) {
            log.error(className + "create", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<DispatchFeeApply> approve(DispatchFeeApply dispatchFeeApply) {
        try {
            Assert.notNull(dispatchFeeApply);
            Assert.notEmpty(dispatchFeeApply.getItems());
            dispatchFeeApply.setApprove(true);
            dispatchFeeApply.setApproveTime(LocalDateTime.now());
            Assert.isTrue(feeApplyMapper.update(dispatchFeeApply) == 1);
            feeApplyItemMapper.batchUpdate(dispatchFeeApply.getItems());
            return ResponseBuilder.success(dispatchFeeApply);
        } catch (Exception e) {
            log.error(className + "approve", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public Response<DispatchFeeApply> update(DispatchFeeApply dispatchFeeApply) {
        try {
            Assert.notNull(dispatchFeeApply);
            Assert.notEmpty(dispatchFeeApply.getItems());
            Assert.isTrue(feeApplyMapper.update(dispatchFeeApply) == 1);
            feeApplyItemMapper.batchUpdate(dispatchFeeApply.getItems());
            return ResponseBuilder.success(dispatchFeeApply);
        } catch (Exception e) {
            log.error(className + "update", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.fail(e);
        }
    }

    @Override
    public ListResponse<DispatchFeeApply> searchFeeApplies(String siteCode, String applyUserId, String applyDate) {
        try {
            return ResponseBuilder.list(feeApplyMapper.searchFeeApplies(siteCode, applyUserId, applyDate));
        } catch (Exception e) {
            log.error(className + "searchFeeApplies", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.listFail(e);
        }
    }

    @Override
    public Response<Map<String, Object>> getMap(String feeApplyId) {
        try {
            Map<String, Object> map = new HashMap();
            if (!StringUtils.hasText(feeApplyId)) {
                return ResponseBuilder.fail("申报ID为空");
            }
            Response<DispatchFeeApply> response = get(feeApplyId);
            if (!response.isSuccess()) {
                return ResponseBuilder.fail(response.getMessage());
            }
            DispatchFeeApply dispatchFeeApply = response.getBody();
            if (null == dispatchFeeApply) {
                return ResponseBuilder.fail("费用申报不存在");
            }

            List<DispatchFeeApplyPic> pics = dispatchFeeApply.getPics();
            //// TODO: 2017/1/6 文件上传
           /* pics.forEach(e -> {
                e.setFilePath(fastHost + e.getFilePath());
                e.setThumbPath(fastHost + e.getThumbPath());
            });*/

            map.put("dispatchFeeApply", dispatchFeeApply);
            List<DispatchFeeApplyItem> items = dispatchFeeApply.getItems();
            if (null == items || 0 == items.size()) {
                return ResponseBuilder.fail("费用申报无明细");
            }
            ListResponse<ExpenseAccount> expenseAccountListResponse = expenseAccountService.findAll();
            if (!expenseAccountListResponse.isSuccess()) {
                return ResponseBuilder.fail(expenseAccountListResponse.getMessage());
            }
            List<ExpenseAccount> expenseAccounts = expenseAccountListResponse.getBody();
            List<DispatchFeeApplyItemDto> feeApplyItemDtos = new ArrayList<>();
            items.stream().forEach(
                    item -> {
                        DispatchFeeApplyItemDto itemDto = (DispatchFeeApplyItemDto) ReflectUtils.convert(item, DispatchFeeApplyItemDto.class);
                        ExpenseAccount expenseAccount = ObjectQuery.findOne(expenseAccounts, "code", itemDto.getAccountCode());
                        if (expenseAccount != null) {
                            itemDto.setAccountName(expenseAccount.getName());
                        }
                        feeApplyItemDtos.add(itemDto);
                    }
            );
            map.put("feeApplyItems", feeApplyItemDtos);

            String approveUserId = dispatchFeeApply.getApproveUserId();
            if (!StringUtils.isEmpty(approveUserId)) {
                Response<User> userResponse = userService.get(approveUserId);
                if (!userResponse.isSuccess()) {
                    return ResponseBuilder.fail(userResponse.getMessage());
                }
                if (userResponse.getBody() != null) {
                    map.put("approveUserName", userResponse.getBody().getFullName());
                }
            }

            return ResponseBuilder.success(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBuilder.fail(e.getMessage());
        }
    }
}
