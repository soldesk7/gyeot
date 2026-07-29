import { request } from './client'

export const recognize = (photo) => {
    const formData = new FormData()
    formData.append('photo', photo)

    return request('/api/v1/recognitions', {
        method: 'POST',
        body: formData,
    })

}