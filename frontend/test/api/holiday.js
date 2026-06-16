import { request } from '@/utils/request.js';

export function getHolidays() {
  return request({ url: '/api/v1/holidays', method: 'GET' });
}

export function addHoliday(date) {
  return request({ url: '/api/v1/holidays', method: 'POST', data: { date: date } });
}

export function deleteHoliday(date) {
  return request({ url: `/api/v1/holidays/${date}`, method: 'DELETE' });
}