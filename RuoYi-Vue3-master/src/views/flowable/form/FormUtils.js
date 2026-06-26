export function extractFormData(formData) {
  const result = {}
  formData.forEach(comp => {
    if (!comp.field) return
    
    let value = comp.defaultValue
    
    if (comp.type === 'user' || comp.type === 'dept') {
      if (value && typeof value === 'object') {
        value = {
          id: value.userId || value.deptId,
          name: value.userName || value.deptName
        }
      }
    } else if (comp.type === 'users' || comp.type === 'depts') {
      if (Array.isArray(value)) {
        value = value.map(item => ({
          id: item.userId || item.deptId,
          name: item.userName || item.deptName
        }))
      }
    }
    
    result[comp.field] = value
  })
  return result
}

export function extractIds(formData, fieldName) {
  const comp = formData.find(c => c.field === fieldName)
  if (!comp) return null
  
  const value = comp.defaultValue
  
  if (comp.type === 'user' || comp.type === 'dept') {
    return value ? (value.userId || value.deptId) : null
  } else if (comp.type === 'users' || comp.type === 'depts') {
    return Array.isArray(value) ? value.map(item => item.userId || item.deptId) : []
  }
  
  return value
}