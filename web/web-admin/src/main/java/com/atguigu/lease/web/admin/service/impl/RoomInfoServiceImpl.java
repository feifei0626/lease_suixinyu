package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private AttrValueMapper attrValueMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private RoomAttrValueService roomAttrValueService;
    @Autowired
    private RoomFacilityService roomFacilityService;
    @Autowired
    private RoomLabelService roomLabelService;
    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;
    @Autowired
    private RoomLeaseTermService roomLeaseTermService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo) {
        //保存和更新的操作不同，更新需要先把原有的删除再保存

        boolean isUpdate = roomSubmitVo.getId() != null;
        //RoomInfo部分的保存和更新    直接调用方法，对其他部分编写自己的业务代码
        super.saveOrUpdate(roomSubmitVo);

        if(isUpdate){
            //其他部分的删除操作
            //1.删除图片列表
            LambdaQueryWrapper<GraphInfo> graphLqw = new LambdaQueryWrapper<>();
            graphLqw.eq(GraphInfo::getItemType, ItemType.ROOM);//根据所属对象类型删除
            graphLqw.eq(GraphInfo::getItemId, roomSubmitVo.getId());//注意这里是根据图片所有对象id删除，而不是图片id
            graphInfoService.remove(graphLqw);
            //2.删除属性信息列表
            LambdaQueryWrapper<RoomAttrValue> attrValueLqw = new LambdaQueryWrapper<>();
            attrValueLqw.eq(RoomAttrValue::getRoomId, roomSubmitVo.getId());
            roomAttrValueService.remove(attrValueLqw);
            //3.删除配套信息列表
            LambdaQueryWrapper<RoomFacility> facilityLqw = new LambdaQueryWrapper<>();
            facilityLqw.eq(RoomFacility::getRoomId, roomSubmitVo.getId());
            roomFacilityService.remove(facilityLqw);
            //4.删除标签信息列表
            LambdaQueryWrapper<RoomLabel> labelLqw = new LambdaQueryWrapper<>();
            labelLqw.eq(RoomLabel::getRoomId, roomSubmitVo.getId());
            roomLabelService.remove(labelLqw);
            //5.删除支付方式列表
            LambdaQueryWrapper<RoomPaymentType> paymentTypeLqw = new LambdaQueryWrapper<>();
            paymentTypeLqw.eq(RoomPaymentType::getRoomId, roomSubmitVo.getId());
            roomPaymentTypeService.remove(paymentTypeLqw);
            //6.删除可选租期列表
            LambdaQueryWrapper<RoomLeaseTerm> leaseTermLqw = new LambdaQueryWrapper<>();
            leaseTermLqw.eq(RoomLeaseTerm::getRoomId, roomSubmitVo.getId());
            roomLeaseTermService.remove(leaseTermLqw);

            //为保持缓存与数据库的数据一致性，更新信息时，删除缓存
            String key = RedisConstant.APP_ROOM_PREFIX + roomSubmitVo.getId();
            redisTemplate.delete(key);
        }

        //其他部分的保存操作
        //1.保存图片列表
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfos = new ArrayList<>();
            for(GraphVo graphVo : graphVoList){
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfos.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfos);
        }

        //2.保存属性信息列表
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if(!CollectionUtils.isEmpty(attrValueIds)){
            ArrayList<RoomAttrValue> roomAttrValues = new ArrayList<>();
            for(Long attrValueId : attrValueIds){
                RoomAttrValue roomAttrValue = RoomAttrValue.builder().build();
                roomAttrValue.setRoomId(roomSubmitVo.getId());
                roomAttrValue.setAttrValueId(attrValueId);
                roomAttrValues.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(roomAttrValues);
        }

        //3.保存配套信息列表
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(facilityInfoIds)){
            ArrayList<RoomFacility> roomFacilities = new ArrayList<>();
            for(Long facilityInfoId : facilityInfoIds){
                RoomFacility roomFacility = RoomFacility.builder().build();
                roomFacility.setRoomId(roomSubmitVo.getId());
                roomFacility.setFacilityId(facilityInfoId);
                roomFacilities.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilities);
        }
        //4.保存标签信息列表
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if(!CollectionUtils.isEmpty(labelInfoIds)){
            ArrayList<RoomLabel> roomLabels = new ArrayList<>();
            for(Long labelInfoId : labelInfoIds){
                RoomLabel roomLabel = RoomLabel.builder().build();
                roomLabel.setRoomId(roomSubmitVo.getId());
                roomLabel.setLabelId(labelInfoId);
                roomLabels.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabels);
        }
        //5.保存支付方式列表
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if(!CollectionUtils.isEmpty(paymentTypeIds)){
            ArrayList<RoomPaymentType> roomPaymentTypes = new ArrayList<>();
            for(Long paymentTypeId : paymentTypeIds){
                RoomPaymentType roomPaymentType = RoomPaymentType.builder().build();
                roomPaymentType.setRoomId(roomSubmitVo.getId());
                roomPaymentType.setPaymentTypeId(paymentTypeId);
                roomPaymentTypes.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypes);
        }
        //6.保存可选租期列表
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if(!CollectionUtils.isEmpty(leaseTermIds)){
            ArrayList<RoomLeaseTerm> roomLeaseTerms = new ArrayList<>();
            for(Long leaseTermId : leaseTermIds){
                RoomLeaseTerm roomLeaseTerm = RoomLeaseTerm.builder().build();
                roomLeaseTerm.setRoomId(roomSubmitVo.getId());
                roomLeaseTerm.setLeaseTermId(leaseTermId);
                roomLeaseTerms.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTerms);
        }

    }

    @Override
    public IPage<RoomItemVo> pageItem(IPage<RoomItemVo> page, RoomQueryVo queryVo) {
        return roomInfoMapper.pageItem(page,queryVo);
    }

    @Override
    public RoomDetailVo getRoomDetailById(Long id) {
        //1.查询RoomInfo
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        if(roomInfo == null){
            return null;
        }
        //2.查询所属公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());

        //3.查询graphInfoList
        List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, id);

        //4.查询attrValueList
        List<AttrValueVo> attrvalueVoList = attrValueMapper.selectListByRoomId(id);

        //5.查询facilityInfoList
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(id);

        //6.查询labelInfoList
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);

        //7.查询paymentTypeList
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);

        //8.查询leaseTermList
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);

        //8.组装结果
        RoomDetailVo adminRoomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo, adminRoomDetailVo);

        adminRoomDetailVo.setApartmentInfo(apartmentInfo);
        adminRoomDetailVo.setGraphVoList(graphVoList);
        adminRoomDetailVo.setAttrValueVoList(attrvalueVoList);
        adminRoomDetailVo.setFacilityInfoList(facilityInfoList);
        adminRoomDetailVo.setLabelInfoList(labelInfoList);
        adminRoomDetailVo.setPaymentTypeList(paymentTypeList);
        adminRoomDetailVo.setLeaseTermList(leaseTermList);

        return adminRoomDetailVo;
    }

    @Override
    public void removeRoomItemVoById(Long id) {

        //删除RoomInfo
        super.removeById(id);

        //1.删除图片列表
        LambdaQueryWrapper<GraphInfo> graphLqw = new LambdaQueryWrapper<>();
        graphLqw.eq(GraphInfo::getItemType, ItemType.ROOM);//根据所属对象类型删除
        graphLqw.eq(GraphInfo::getItemId, id);//注意这里是根据图片所有对象id删除，而不是图片id
        graphInfoService.remove(graphLqw);
        //2.删除属性信息列表
        LambdaQueryWrapper<RoomAttrValue> attrValueLqw = new LambdaQueryWrapper<>();
        attrValueLqw.eq(RoomAttrValue::getRoomId, id);
        roomAttrValueService.remove(attrValueLqw);
        //3.删除配套信息列表
        LambdaQueryWrapper<RoomFacility> facilityLqw = new LambdaQueryWrapper<>();
        facilityLqw.eq(RoomFacility::getRoomId, id);
        roomFacilityService.remove(facilityLqw);
        //4.删除标签信息列表
        LambdaQueryWrapper<RoomLabel> labelLqw = new LambdaQueryWrapper<>();
        labelLqw.eq(RoomLabel::getRoomId, id);
        roomLabelService.remove(labelLqw);
        //5.删除支付方式列表
        LambdaQueryWrapper<RoomPaymentType> paymentTypeLqw = new LambdaQueryWrapper<>();
        paymentTypeLqw.eq(RoomPaymentType::getRoomId, id);
        roomPaymentTypeService.remove(paymentTypeLqw);
        //6.删除可选租期列表
        LambdaQueryWrapper<RoomLeaseTerm> leaseTermLqw = new LambdaQueryWrapper<>();
        leaseTermLqw.eq(RoomLeaseTerm::getRoomId, id);
        roomLeaseTermService.remove(leaseTermLqw);

        //为保持缓存与数据库的数据一致性，更新信息时，删除缓存
        String key = RedisConstant.APP_ROOM_PREFIX + id;
        redisTemplate.delete(key);
    }
}




