import React, { useState, useEffect } from 'react';
import {
  Container, Paper, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Button, Typography, Box, Chip, Dialog,
  DialogTitle, DialogContent, DialogActions, TextField, MenuItem,
  Select, FormControl, InputLabel, IconButton, Alert, Collapse,
} from '@mui/material';
import {
  Add as AddIcon, Cancel as CancelIcon,
  KeyboardArrowDown, KeyboardArrowUp,
} from '@mui/icons-material';
import { orderAPI, customerAPI, productAPI } from '../../services/api';

const getStatusColor = (status) => {
  const colors = {
    PENDING: 'warning', CONFIRMED: 'info', PROCESSING: 'primary',
    SHIPPED: 'secondary', DELIVERED: 'success', CANCELLED: 'error',
  };
  return colors[status] || 'default';
};

const formatDate = (dateStr) => {
  if (!dateStr) return 'N/A';
  try {
    // Handle Java LocalDateTime array format [2026, 2, 11, 5, 54, 29]
    if (Array.isArray(dateStr)) {
      const [year, month, day] = dateStr;
      const months = ['Jan','Feb','Mar','Apr','May','Jun',
                      'Jul','Aug','Sep','Oct','Nov','Dec'];
      return `${String(day).padStart(2,'0')} ${months[month-1]} ${year}`;
    }
    // Handle string format
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return String(dateStr).substring(0, 10);
    return date.toLocaleDateString('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  } catch (e) {
    return 'N/A';
  }
};

const OrderRow = ({ order, onRefresh }) => {
  const [open, setOpen] = useState(false);

  const handleCancel = async () => {
    if (window.confirm('Cancel this order?')) {
      try {
        await orderAPI.cancel(order.id, 'Cancelled by user');
        onRefresh();
      } catch (e) {
        alert('Cannot cancel: ' + (e.response?.data?.message || e.message));
      }
    }
  };

  return (
    <>
      <TableRow hover>
        <TableCell>
          <IconButton size="small" onClick={() => setOpen(!open)}>
            {open ? <KeyboardArrowUp /> : <KeyboardArrowDown />}
          </IconButton>
        </TableCell>
        <TableCell><strong>{order.orderNumber}</strong></TableCell>
        <TableCell>{order.customerName || 'N/A'}</TableCell>
        <TableCell>{formatDate(order.orderDate)}</TableCell>
        <TableCell>₹{Number(order.totalAmount || 0).toFixed(2)}</TableCell>
        <TableCell>
          <Chip label={order.status} color={getStatusColor(order.status)} size="small" />
        </TableCell>
        <TableCell>
          {order.status !== 'CANCELLED' && order.status !== 'DELIVERED' && (
            <IconButton size="small" color="error" onClick={handleCancel}>
              <CancelIcon />
            </IconButton>
          )}
        </TableCell>
      </TableRow>
      <TableRow>
        <TableCell colSpan={7} sx={{ py: 0 }}>
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Box sx={{ m: 2 }}>
              <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
                Order Items
              </Typography>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Product</TableCell>
                    <TableCell>SKU</TableCell>
                    <TableCell>Qty</TableCell>
                    <TableCell>Unit Price</TableCell>
                    <TableCell>Total</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(order.items || []).map((item, i) => (
                    <TableRow key={i}>
                      <TableCell>{item.productName}</TableCell>
                      <TableCell>{item.productSku}</TableCell>
                      <TableCell>{item.quantity}</TableCell>
                      <TableCell>₹{item.unitPrice}</TableCell>
                      <TableCell>₹{item.lineTotal}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
              <Box sx={{ mt: 1, textAlign: 'right' }}>
                <Typography variant="body2">Subtotal: ₹{Number(order.subtotal||0).toFixed(2)}</Typography>
                <Typography variant="body2">Tax (18%): ₹{Number(order.taxAmount||0).toFixed(2)}</Typography>
                <Typography variant="subtitle1" fontWeight="bold">
                  Total: ₹{Number(order.totalAmount||0).toFixed(2)}
                </Typography>
              </Box>
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>
    </>
  );
};

const OrderList = () => {
  const [orders, setOrders] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [openDialog, setOpenDialog] = useState(false);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({
    customerId: '',
    items: [{ productId: '', quantity: 1 }],
  });

  useEffect(() => { fetchAll(); }, []);

  const fetchAll = async () => {
    try {
      const [ordersRes, customersRes, productsRes] = await Promise.all([
        orderAPI.getAll(),
        customerAPI.getAll(),
        productAPI.getAll(),
      ]);
      setOrders(Array.isArray(ordersRes.data) ? ordersRes.data : []);
      setCustomers(Array.isArray(customersRes.data) ? customersRes.data : []);
      setProducts(Array.isArray(productsRes.data) ? productsRes.data : []);
    } catch (e) {
      console.error('Fetch error:', e);
    }
  };

  const handleSubmit = async () => {
    try {
      setError('');
      if (!formData.customerId) { setError('Please select a customer'); return; }
      if (formData.items.some(i => !i.productId)) { setError('Please select all products'); return; }

      const orderData = {
        customer: { id: parseInt(formData.customerId) },
        orderDate: new Date().toISOString(),
        status: 'PENDING',
        items: formData.items.map(item => {
          const product = products.find(p => p.id === parseInt(item.productId));
          return {
            product: { id: parseInt(item.productId) },
            quantity: parseInt(item.quantity),
            unitPrice: product?.price || 0,
            productName: product?.name || '',
            productSku: product?.sku || ''
          };
        })
      };

      await orderAPI.create(orderData);
      setOpenDialog(false);
      setFormData({ customerId: '', items: [{ productId: '', quantity: 1 }] });
      fetchAll();
    } catch (e) {
      setError(e.response?.data?.message || e.message || 'Failed to create order');
    }
  };

  const updateItem = (index, field, value) => {
    const newItems = [...formData.items];
    newItems[index][field] = value;
    setFormData({ ...formData, items: newItems });
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4">Orders ({orders.length})</Typography>
        <Button variant="contained" startIcon={<AddIcon />}
          onClick={() => { setError(''); setOpenDialog(true); }}>
          Create Order
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow sx={{ bgcolor: 'primary.main' }}>
              <TableCell sx={{ color: 'white' }} />
              <TableCell sx={{ color: 'white' }}>Order #</TableCell>
              <TableCell sx={{ color: 'white' }}>Customer</TableCell>
              <TableCell sx={{ color: 'white' }}>Date</TableCell>
              <TableCell sx={{ color: 'white' }}>Total</TableCell>
              <TableCell sx={{ color: 'white' }}>Status</TableCell>
              <TableCell sx={{ color: 'white' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {orders.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  No orders yet. Create your first order!
                </TableCell>
              </TableRow>
            ) : (
              orders.map(order => (
                <OrderRow key={order.id} order={order} onRefresh={fetchAll} />
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>Create New Order</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <FormControl fullWidth margin="normal">
            <InputLabel>Customer *</InputLabel>
            <Select value={formData.customerId}
              onChange={(e) => setFormData({ ...formData, customerId: e.target.value })}
              label="Customer *">
              {customers.map(c => (
                <MenuItem key={c.id} value={c.id}>
                  {c.firstName} {c.lastName} ({c.email})
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>Order Items</Typography>
          {formData.items.map((item, index) => (
            <Box key={index} sx={{ display: 'flex', gap: 2, mb: 2, alignItems: 'center' }}>
              <FormControl sx={{ flex: 2 }}>
                <InputLabel>Product *</InputLabel>
                <Select value={item.productId}
                  onChange={(e) => updateItem(index, 'productId', e.target.value)}
                  label="Product *">
                  {products.map(p => (
                    <MenuItem key={p.id} value={p.id}>
                      {p.name} - ₹{p.price} (Stock: {p.quantityInStock})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <TextField sx={{ flex: 1 }} label="Qty" type="number"
                value={item.quantity}
                onChange={(e) => updateItem(index, 'quantity', e.target.value)}
                inputProps={{ min: 1 }} />
              {formData.items.length > 1 && (
                <Button color="error" size="small"
                  onClick={() => setFormData({
                    ...formData,
                    items: formData.items.filter((_, i) => i !== index)
                  })}>Remove</Button>
              )}
            </Box>
          ))}
          <Button variant="outlined" size="small"
            onClick={() => setFormData({
              ...formData,
              items: [...formData.items, { productId: '', quantity: 1 }]
            })}>+ Add Item</Button>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit}>Create Order</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default OrderList;
