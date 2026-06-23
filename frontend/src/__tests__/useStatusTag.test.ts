import { describe, it, expect } from 'vitest'
import { useStatusTag, appointmentStatusMap, orderStatusMap } from '@/composables/useStatusTag'

describe('useStatusTag', () => {
  describe('appointmentStatusMap', () => {
    it('should return correct label for pending status', () => {
      const { getLabel } = useStatusTag(appointmentStatusMap)
      expect(getLabel(0)).toBe('待确认')
    })

    it('should return correct type for confirmed status', () => {
      const { getType } = useStatusTag(appointmentStatusMap)
      expect(getType(1)).toBe('success')
    })

    it('should return correct tag info for cancelled status', () => {
      const { getTagInfo } = useStatusTag(appointmentStatusMap)
      const info = getTagInfo(3)
      expect(info.label).toBe('已取消')
      expect(info.type).toBe('danger')
    })

    it('should return default for unknown status', () => {
      const { getLabel, getType } = useStatusTag(appointmentStatusMap)
      expect(getLabel(999)).toBe('999')
      expect(getType(999)).toBe('info')
    })
  })

  describe('orderStatusMap', () => {
    it('should return correct label for pending payment', () => {
      const { getLabel } = useStatusTag(orderStatusMap)
      expect(getLabel(0)).toBe('待支付')
    })

    it('should return correct type for paid status', () => {
      const { getType } = useStatusTag(orderStatusMap)
      expect(getType(1)).toBe('success')
    })

    it('should return correct tag info for refunded status', () => {
      const { getTagInfo } = useStatusTag(orderStatusMap)
      const info = getTagInfo(5)
      expect(info.label).toBe('已退款')
      expect(info.type).toBe('danger')
    })
  })
})
