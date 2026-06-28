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

describe('HomePage', () => {
  it('shows main user flow on homepage', async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/', component: Home }],
    })
    router.push('/')
    await router.isReady()

    const wrapper = mount(Home, {
      global: {
        plugins: [createPinia(), router],
        stubs: {
          'el-icon': true,
          'el-button': true,
          'el-empty': true,
          teleport: true,
        },
      },
    })

    expect(wrapper.text()).toContain('用户操作主链路')
    expect(wrapper.text()).toContain('浏览套餐')
    expect(wrapper.text()).toContain('确认预约')
  })
})
