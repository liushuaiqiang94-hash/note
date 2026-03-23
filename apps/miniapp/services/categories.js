const request = require('../utils/request');

function listCategories() {
  return request.get('/categories');
}

function createCategory(payload) {
  return request.post('/categories', payload);
}

function updateCategory(categoryId, payload) {
  return request.put(`/categories/${categoryId}`, payload);
}

function deleteCategory(categoryId) {
  return request.del(`/categories/${categoryId}`);
}

module.exports = {
  listCategories,
  createCategory,
  updateCategory,
  deleteCategory,
};
