const request = require('../utils/request');

function listTasks(params) {
  return request.get('/tasks', params);
}

function getTask(taskId) {
  return request.get(`/tasks/${taskId}`);
}

function createTask(payload) {
  return request.post('/tasks', payload);
}

function updateTask(taskId, payload) {
  return request.put(`/tasks/${taskId}`, payload);
}

function updateTaskStatus(taskId, status) {
  return request.patch(`/tasks/${taskId}/status`, { status });
}

function deleteTask(taskId) {
  return request.del(`/tasks/${taskId}`);
}

function getTodayTasks() {
  return request.get('/tasks/today');
}

function getRecycleBin(params) {
  return request.get('/tasks/recycle-bin', params);
}

function restoreTask(taskId) {
  return request.post(`/tasks/${taskId}/restore`);
}

module.exports = {
  listTasks,
  getTask,
  createTask,
  updateTask,
  updateTaskStatus,
  deleteTask,
  getTodayTasks,
  getRecycleBin,
  restoreTask,
};
