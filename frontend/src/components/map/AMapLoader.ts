declare global {
  interface Window {
    AMap?: any
    _AMapSecurityConfig?: {
      securityJsCode?: string
    }
  }
}

let amapReadyPromise: Promise<any> | null = null

function getAmapScriptUrl(key: string) {
  const query = new URLSearchParams({
    key,
    v: '2.0',
    plugin: 'AMap.Geocoder',
  })
  return `https://webapi.amap.com/maps?${query.toString()}`
}

export function loadAmapSdk() {
  if (window.AMap) return Promise.resolve(window.AMap)
  if (amapReadyPromise) return amapReadyPromise

  const key = import.meta.env.VITE_AMAP_KEY as string | undefined
  if (!key) {
    return Promise.reject(new Error('缺少 VITE_AMAP_KEY，请在 frontend/.env 中配置'))
  }

  amapReadyPromise = new Promise((resolve, reject) => {
    const existingScript = document.querySelector('script[data-amap-sdk="true"]') as HTMLScriptElement | null
    if (existingScript) {
      existingScript.addEventListener('load', () => {
        if (window.AMap) resolve(window.AMap)
        else reject(new Error('高德地图 SDK 加载失败'))
      })
      existingScript.addEventListener('error', () => reject(new Error('高德地图 SDK 加载失败')))
      return
    }

    const script = document.createElement('script')
    script.src = getAmapScriptUrl(key)
    script.async = true
    script.defer = true
    script.dataset.amapSdk = 'true'
    script.onload = () => {
      if (window.AMap) resolve(window.AMap)
      else reject(new Error('高德地图 SDK 加载失败'))
    }
    script.onerror = () => reject(new Error('高德地图 SDK 加载失败'))
    document.head.appendChild(script)
  }).catch((err) => {
    amapReadyPromise = null
    return Promise.reject(err)
  })

  return amapReadyPromise
}

export {}
