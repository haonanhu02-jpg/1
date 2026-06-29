import request from '@/utils/request'

const { get, post, put, delt } = request

export const getPhaseOneScope = () => get('/phaseOne/scope')
export const getPhaseOneMenus = () => get('/phaseOne/menus')
export const getStatisticsSummary = () => get('/phaseOne/statistics/summary')
export const getStatisticsReport = (data) => get('/phaseOne/statistics/report', data)
export const getAcceptanceSummary = () => get('/phaseOne/acceptance/summary')

export const getEmployeePoolList = (data) => get('/phaseOne/employeePool/findPage', data)
export const saveEmployeePool = (data) => post('/phaseOne/employeePool/save', data)
export const updateEmployeePool = (data) => put('/phaseOne/employeePool/update', data)
export const deleteEmployeePool = (ids) => delt(`/phaseOne/employeePool/${ids}`)

export const getGroupPoolList = (data) => get('/phaseOne/groupPool/findPage', data)
export const saveGroupPool = (data) => post('/phaseOne/groupPool/save', data)
export const updateGroupPool = (data) => put('/phaseOne/groupPool/update', data)
export const deleteGroupPool = (ids) => delt(`/phaseOne/groupPool/${ids}`)
export const bindGroupPoolChat = (data) => post('/phaseOne/groupPool/bindChat', data)

export const getGroupRouteRuleList = (data) => get('/phaseOne/groupRouteRule/findPage', data)
export const saveGroupRouteRule = (data) => post('/phaseOne/groupRouteRule/save', data)
export const updateGroupRouteRule = (data) => put('/phaseOne/groupRouteRule/update', data)
export const deleteGroupRouteRule = (ids) => delt(`/phaseOne/groupRouteRule/${ids}`)
export const matchGroupRoutePreview = (data) => post('/phaseOne/groupRouteRule/matchPreview', data)

export const getCustomerChatRelationList = (data) => get('/phaseOne/customerChatRelation/findPage', data)
export const getCustomerRouteLogList = (data) => get('/phaseOne/customerRouteLog/findPage', data)

export const getAlertLogList = (data) => get('/phaseOne/alertLog/findPage', data)
export const handleAlertLog = (data) => put('/phaseOne/alertLog/handle', data)

export const getFailedTaskList = (data) => get('/phaseOne/failedTask/findPage', data)
export const retryFailedTask = (id) => post(`/phaseOne/failedTask/retry/${id}`)

export const getApiCallLogList = (data) => get('/phaseOne/apiCallLog/findPage', data)
export const getBusinessLogList = (data) => get('/phaseOne/businessLog/findPage', data)

export const exportStatisticsCsv = (data) => get('/phaseOne/statistics/exportCsv', data, { responseType: 'blob' })
export const exportCustomerChatRelationCsv = (data) => get('/phaseOne/customerChatRelation/exportCsv', data, { responseType: 'blob' })
export const exportAlertLogCsv = (data) => get('/phaseOne/alertLog/exportCsv', data, { responseType: 'blob' })
export const exportBusinessLogCsv = (data) => get('/phaseOne/businessLog/exportCsv', data, { responseType: 'blob' })

export const mockScan = (data) => post('/phase-one/mock/scan', data)
export const mockAddContact = (data) => post('/phase-one/mock/add-contact', data)
export const mockJoinGroup = (data) => post('/phase-one/mock/join-group', data)
export const mockLeaveGroup = (data) => post('/phase-one/mock/leave-group', data)

// 复用 Iyque 原有基础数据接口，供一期新增页面选择活码、客户群、员工。
export const getUserCodeOptions = () => get('/iyQue/findIYqueUserCodeKvs')
export const getGroupChatOptions = (data) => get('/iYqueChat/findIYqueChatPage', data)
export const getUserOptions = (data) => get('/iYqueUser/findIYqueUserPage', data)
