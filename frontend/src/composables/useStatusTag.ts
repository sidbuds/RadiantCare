export type TagType = '' | 'success' | 'warning' | 'info' | 'danger'

export interface StatusMapItem {
  label: string
  type: TagType
}

export type StatusMap = Record<string | number, StatusMapItem>

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

export const appointmentStatusMap: StatusMap = {
  0: { label: '待支付', type: 'info' },
  1: { label: '待体检', type: 'success' },
  2: { label: '已体检', type: '' },
  3: { label: '已关闭', type: 'danger' },
  4: { label: '爽约', type: 'danger' },
}

export const orderStatusMap: StatusMap = {
  0: { label: '待支付', type: 'warning' },
  1: { label: '已支付', type: 'success' },
  2: { label: '已退款', type: 'danger' },
  3: { label: '退款中', type: 'warning' },
  4: { label: '已完成', type: '' },
}

export const examTaskStatusMap: StatusMap = {
  0: { label: '待执行', type: 'info' },
  1: { label: '执行中', type: 'warning' },
  2: { label: '已完成', type: 'success' },
}

export const examItemStatusMap: StatusMap = {
  0: { label: '待检查', type: 'info' },
  1: { label: '检查中', type: 'warning' },
  2: { label: '已完成', type: 'success' },
  4: { label: '待复检', type: 'danger' },
}

export const reportStatusMap: StatusMap = {
  1: { label: '待审核', type: 'warning' },
  3: { label: '已发布', type: 'success' },
  4: { label: '管理员处理', type: 'danger' },
}

export const consultationStatusMap: StatusMap = {
  0: { label: '待回复', type: 'warning' },
  1: { label: '已回复', type: 'success' },
  2: { label: '已关闭', type: 'info' },
}

export const refundStatusMap: StatusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' },
  3: { label: '退款中', type: 'warning' },
  4: { label: '已退款', type: 'success' },
}

export const abnormalLevelMap: StatusMap = {
  0: { label: '正常', type: 'success' },
  1: { label: '偏低', type: 'info' },
  2: { label: '偏高', type: 'warning' },
  3: { label: '高危', type: 'danger' },
}
