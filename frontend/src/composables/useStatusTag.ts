/** 状态标签类型 */
export type TagType = '' | 'success' | 'warning' | 'info' | 'danger'

/** 状态映射配置 */
export interface StatusMapItem {
  label: string
  type: TagType
}

/** 通用状态映射类型 */
export type StatusMap = Record<string | number, StatusMapItem>

/**
 * 通用状态标签 composable
 * @param statusMap 状态映射表
 */
export function useStatusTag(statusMap: StatusMap) {
  function getTagInfo(status: string | number): StatusMapItem {
    return statusMap[status] || { label: String(status), type: 'info' }
  }

  function getLabel(status: string | number): string {
    return getTagInfo(status).label
  }

  function getType(status: string | number): TagType {
    return getTagInfo(status).type
  }

  return { getTagInfo, getLabel, getType }
}

// ==================== 预定义状态映射 ====================

/** 预约状态 */
export const appointmentStatusMap: StatusMap = {
  0: { label: '待确认', type: 'info' },
  1: { label: '已确认', type: 'success' },
  2: { label: '已完成', type: '' },
  3: { label: '已取消', type: 'danger' },
}

/** 订单状态 */
export const orderStatusMap: StatusMap = {
  0: { label: '待支付', type: 'warning' },
  1: { label: '已支付', type: 'success' },
  2: { label: '已完成', type: '' },
  3: { label: '已取消', type: 'info' },
  4: { label: '退款中', type: 'danger' },
  5: { label: '已退款', type: 'danger' },
}

/** 体检任务状态 */
export const examTaskStatusMap: StatusMap = {
  0: { label: '待开始', type: 'info' },
  1: { label: '进行中', type: 'warning' },
  2: { label: '已完成', type: 'success' },
}

/** 体检项状态 */
export const examItemStatusMap: StatusMap = {
  0: { label: '待检查', type: 'info' },
  1: { label: '检查中', type: 'warning' },
  2: { label: '已完成', type: 'success' },
  4: { label: '待复检', type: 'danger' },
}

/** 报告状态 */
export const reportStatusMap: StatusMap = {
  0: { label: '待审核', type: 'info' },
  1: { label: '待审核', type: 'info' },
  2: { label: '已审核', type: 'warning' },
  3: { label: '已发布', type: 'success' },
}

/** 咨询状态 */
export const consultationStatusMap: StatusMap = {
  0: { label: '待回复', type: 'warning' },
  1: { label: '已回复', type: 'success' },
  2: { label: '已关闭', type: 'info' },
}

/** 退款申请状态 */
export const refundStatusMap: StatusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' },
}

/** 异常等级 */
export const abnormalLevelMap: StatusMap = {
  0: { label: '正常', type: 'success' },
  1: { label: '偏低', type: 'info' },
  2: { label: '偏高', type: 'warning' },
  3: { label: '高危', type: 'danger' },
}
