import { request } from './client'

export const fetchHospitals = (lat, lng) => {
    return request(`/api/v1/emergency-rooms?lat=${lat}&lng=${lng}`)
}
