import React, { useState, useEffect } from 'react';
import {
  Container, Paper, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Button, Typography, Box, Chip, IconButton,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  Alert, Snackbar, Tooltip,
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, Warning as WarningIcon } from '@mui/icons-material';
import { productAPI } from '../../services/api';

const defaultForm = { sku: '', name: '', description: '', price: '', quantityInStock: '' };

const ProductList = () => {
  const [products, setProducts] = useState([]);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [formData, setFormData] = useState(defaultForm);
  const [error, setError] = useState('');
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  useEffect(() => { fetchProducts(); }, []);

  const fetchProducts = async () => {
    try {
      const res = await productAPI.getAll();
      const data = Array.isArray(res.data) ? res.data : [];
      setProducts(data.filter(p => !p.deleted));
    } catch (e) {
      console.error(e);
      setProducts([]);
    }
  };

  const handleOpen = (product = null) => {
    setError('');
    setEditingProduct(product);
    setFormData(product ? {
      sku: product.sku || '',
      name: product.name || '',
      description: product.description || '',
      price: product.price || '',
      quantityInStock: product.quantityInStock || '',
    } : defaultForm);
    setOpenDialog(true);
  };

  const handleSubmit = async () => {
    try {
      setError('');
      if (!formData.name || !formData.price || !formData.quantityInStock) {
        setError('Name, price and quantity are required');
        return;
      }
      if (!editingProduct && !formData.sku) {
        setError('SKU is required');
        return;
      }
      const data = {
        ...formData,
        price: parseFloat(formData.price),
        quantityInStock: parseInt(formData.quantityInStock),
      };
      if (editingProduct) {
        await productAPI.update(editingProduct.id, data);
        setSnackbar({ open: true, message: 'Product updated!', severity: 'success' });
      } else {
        await productAPI.create(data);
        setSnackbar({ open: true, message: 'Product created!', severity: 'success' });
      }
      setOpenDialog(false);
      fetchProducts();
    } catch (e) {
      setError(e.response?.data?.message || 'Error saving product');
    }
  };

  const handleDelete = async (id, name) => {
    if (window.confirm(`Delete product "${name}"?`)) {
      try {
        await productAPI.delete(id);
        setSnackbar({ open: true, message: 'Product deleted!', severity: 'success' });
        fetchProducts();
      } catch (e) {
        setSnackbar({ open: true, message: 'Error: ' + (e.response?.data?.message || e.message), severity: 'error' });
      }
    }
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4">Products ({products.length})</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpen()}>
          Add Product
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow sx={{ bgcolor: 'primary.main' }}>
              <TableCell sx={{ color: 'white' }}>SKU</TableCell>
              <TableCell sx={{ color: 'white' }}>Name</TableCell>
              <TableCell sx={{ color: 'white' }}>Price</TableCell>
              <TableCell sx={{ color: 'white' }}>Stock</TableCell>
              <TableCell sx={{ color: 'white' }}>Available</TableCell>
              <TableCell sx={{ color: 'white' }}>Status</TableCell>
              <TableCell sx={{ color: 'white' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {products.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  No products yet. Add your first product!
                </TableCell>
              </TableRow>
            ) : products.map(product => (
              <TableRow key={product.id} hover>
                <TableCell><code>{product.sku}</code></TableCell>
                <TableCell>{product.name}</TableCell>
                <TableCell>₹{Number(product.price).toFixed(2)}</TableCell>
                <TableCell>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    {product.quantityInStock}
                    {product.quantityInStock <= 10 && (
                      <Tooltip title="Low stock!">
                        <WarningIcon color="warning" fontSize="small" />
                      </Tooltip>
                    )}
                  </Box>
                </TableCell>
                <TableCell>{(product.quantityInStock || 0) - (product.reservedQuantity || 0)}</TableCell>
                <TableCell>
                  <Chip
                    label={product.active ? 'Active' : 'Inactive'}
                    color={product.active ? 'success' : 'default'}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <IconButton size="small" color="primary" onClick={() => handleOpen(product)} title="Edit">
                    <EditIcon />
                  </IconButton>
                  <IconButton size="small" color="error"
                    onClick={() => handleDelete(product.id, product.name)} title="Delete">
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingProduct ? 'Edit Product' : 'Add New Product'}</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 2, mt: 1 }}>{error}</Alert>}
          <TextField fullWidth margin="normal" label="SKU *"
            value={formData.sku}
            onChange={(e) => setFormData({ ...formData, sku: e.target.value })}
            disabled={!!editingProduct}
            helperText="Unique product code e.g. PEN-001, LAPTOP-PRO" />
          <TextField fullWidth margin="normal" label="Product Name *"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })} />
          <TextField fullWidth margin="normal" label="Description"
            multiline rows={2} value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })} />
          <TextField fullWidth margin="normal" label="Price (₹) *"
            type="number" value={formData.price}
            onChange={(e) => setFormData({ ...formData, price: e.target.value })} />
          <TextField fullWidth margin="normal" label="Quantity in Stock *"
            type="number" value={formData.quantityInStock}
            onChange={(e) => setFormData({ ...formData, quantityInStock: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit}>
            {editingProduct ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snackbar.open} autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity} onClose={() => setSnackbar({ ...snackbar, open: false })}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default ProductList;
