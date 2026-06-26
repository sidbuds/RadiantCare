import { describe, it, expect } from 'vitest'
import {
  useStatusTag,
  appointmentStatusMap,
  orderStatusMap,
  refundStatusMap,
} from '@/composables/useStatusTag'

describe('useStatusTag', () => {
  describe('appointmentStatusMap', () => {
    it('returns correct label for pending status', () => {
      const { getLabel } = useStatusTag(appointmentStatusMap)
      expect(getLabel(0)).toBe('待支付')
    })

    it('returns correct type for checkup-pending status', () => {
      const { getType } = useStatusTag(appointmentStatusMap)
      expect(getType(1)).toBe('success')
    })

    it('returns correct tag info for closed status', () => {
      const { getTagInfo } = useStatusTag(appointmentStatusMap)
      const info = getTagInfo(3)
      expect(info.label).toBe('已关闭')
      expect(info.type).toBe('danger')
    })

    it('returns default for unknown status', () => {
      const { getLabel, getType } = useStatusTag(appointmentStatusMap)
      expect(getLabel(999)).toBe('999')
      expect(getType(999)).toBe('info')
    })
  })

  describe('orderStatusMap', () => {
    it('returns correct label for pending payment', () => {
      const { getLabel } = useStatusTag(orderStatusMap)
      expect(getLabel(0)).toBe('待支付')
    })

    it('returns correct type for paid status', () => {
      const { getType } = useStatusTag(orderStatusMap)
      expect(getType(1)).toBe('success')
    })

    it('returns correct tag info for refunded status', () => {
      const { getTagInfo } = useStatusTag(orderStatusMap)
      const info = getTagInfo(2)
      expect(info.label).toBe('已退款')
      expect(info.type).toBe('danger')
    })

    it('keeps status 4 as completed', () => {
      const { getLabel } = useStatusTag(orderStatusMap)
      expect(getLabel(4)).toBe('已完成')
    })
  })

  describe('refundStatusMap', () => {
    it('returns refunded label for status 4', () => {
      const { getLabel, getType } = useStatusTag(refundStatusMap)
      expect(getLabel(4)).toBe('已退款')
      expect(getType(4)).toBe('success')
    })
  })
})
