import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import { describe, it, expect, vi } from 'vitest'
import Home from '@/views/guest/Home.vue'

vi.mock('@/api/public', () => ({
  listPackages: vi.fn().mockResolvedValue({
    data: [
      { id: 1, packageCode: 'P001', packageName: '标准套餐', category: '基础', price: 199, remark: '适合年度体检' },
    ],
  }),
}))

vi.mock('@/api/appointment', () => ({
  getMyAppointments: vi.fn().mockResolvedValue({ data: [] }),
}))

vi.mock('@/api/report', () => ({
  getMyReports: vi.fn().mockResolvedValue({ data: [] }),
}))

vi.mock('@/api/consultation', () => ({
  getMyConsultations: vi.fn().mockResolvedValue({ data: [] }),
}))

describe('HomePage', () => {
  it('shows the homepage flow and task status summary', async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', component: Home },
        { path: '/login', component: { template: '<div />' } },
        { path: '/packages', component: { template: '<div />' } },
        { path: '/guide', component: { template: '<div />' } },
      ],
    })
    router.push('/')
    await router.isReady()

    const wrapper = mount(Home, {
      global: {
        plugins: [createPinia(), router],
        stubs: {
          'el-icon': true,
          'el-button': true,
        },
      },
    })

    const text = wrapper.text()
    expect(text).toContain('您的健康管理流程')
    expect(text).toContain('浏览套餐')
    expect(text).toContain('选择中心')
    expect(text).toContain('选择时间')
    expect(text).toContain('确认预约')
    expect(text).toContain('查看报告')
    expect(wrapper.find('.flow-badge').exists()).toBe(false)
    expect(text).toContain('登录后查看个人进度')
    expect(text).toContain('我的预约')
    expect(text).toContain('体检报告')
    expect(text).toContain('医生咨询')
    expect(text).not.toContain('用户操作主链路')
  })
})
