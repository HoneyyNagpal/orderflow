import React, { useState, useEffect } from 'react';
import {
  Container, Paper, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Button, Typography, Box, Chip, IconButton,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField, Alert, Snackbar,
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { customerAPI } from '../../services/api';

const defaultForm = { firstName: '', lastName: '', email: '', phoneNumber: '' };

const CustomerList = () => {
  const [customers, setCustomers] = useState([]);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState(null);
  const [formData, setFormData] = useState(defaultForm);
  const [error, setError] = useState('');
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  useEffect(() => { fetchCustomers(); }, []);

  const fetchCustomers = async () => {
    try {
      const res = await customerAPI.getAll();
      const data = Array.isArray(res.data) ? res.data : [];
      // Filter out soft-deleted
      setCustomers(data.filter(c => c.active !== false && !c.deleted));
    } catch (e) {
      console.error(e);
      setCustomers([]);
    }
  };

  const handleOpen = (customer = null) => {
    setError('');
    setEditingCustomer(customer);
    setFormData(customer ? {
      firstName: customer.firstName || '',
      lastName: customer.lastName || '',
      email: customer.email || '',
      phoneNumber: customer.phoneNumber || '',
    } : defaultForm);
    setOpenDialog(true);
  };

  const handleSubmit = async () => {
    try {
      setError('');
      if (!formData.firstName || !formData.lastName || !formData.email) {
        setError('First name, last name and email are required');
        return;
      }
      if (editingCustomer) {
        await customerAPI.update(editingCustomer.id, formData);
        setSnackbar({ open: true, message: 'Customer updated!', severity: 'success' });
      } else {
        await customerAPI.create(formData);
        setSnackbar({ open: true, message: 'Customer created!', severity: 'success' });
      }
      setOpenDialog(false);
      fetchCustomers();
    } catch (e) {
      setError(e.response?.data?.message || 'Error saving customer');
    }
  };

  const handleDelete = async (id, name) => {
    if (window.confirm(`Delete customer "${name}"? This cannot be undone.`)) {
      try {
        await customerAPI.delete(id);
        setSnackbar({ open: true, message: 'Customer deleted!', severity: 'success' });
        fetchCustomers();
      } catch (e) {
        setSnackbar({ open: true, message: 'Error deleting customer: ' + (e.response?.data?.message || e.message), severity: 'error' });
      }
    }
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4">Customers ({customers.length})</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => handleOpen()}>
          Add Customer
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow sx={{ bgcolor: 'primary.main' }}>
              <TableCell sx={{ color: 'white' }}>ID</TableCell>
              <TableCell sx={{ color: 'white' }}>Name</TableCell>
              <TableCell sx={{ color: 'white' }}>Email</TableCell>
              <TableCell sx={{ color: 'white' }}>Phone</TableCell>
              <TableCell sx={{ color: 'white' }}>Segment</TableCell>
              <TableCell sx={{ color: 'white' }}>Status</TableCell>
              <TableCell sx={{ color: 'white' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {customers.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  No customers yet. Add your first customer!
                </TableCell>
              </TableRow>
            ) : customers.map(customer => (
              <TableRow key={customer.id} hover>
                <TableCell>{customer.id}</TableCell>
                <TableCell>{customer.firstName} {customer.lastName}</TableCell>
                <TableCell>{customer.email}</TableCell>
                <TableCell>{customer.phoneNumber || '-'}</TableCell>
                <TableCell>
                  <Chip
                    label={customer.segment || 'REGULAR'}
                    color={customer.segment === 'VIP' ? 'primary' : customer.segment === 'PREMIUM' ? 'secondary' : 'default'}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Chip
                    label={customer.active ? 'Active' : 'Inactive'}
                    color={customer.active ? 'success' : 'default'}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <IconButton size="small" color="primary" onClick={() => handleOpen(customer)} title="Edit">
                    <EditIcon />
                  </IconButton>
                  <IconButton size="small" color="error"
                    onClick={() => handleDelete(customer.id, `${customer.firstName} ${customer.lastName}`)}
                    title="Delete">
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingCustomer ? 'Edit Customer' : 'Add New Customer'}</DialogTitle>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 2, mt: 1 }}>{error}</Alert>}
          <TextField fullWidth margin="normal" label="First Name *" value={formData.firstName}
            onChange={(e) => setFormData({ ...formData, firstName: e.target.value })} />
          <TextField fullWidth margin="normal" label="Last Name *" value={formData.lastName}
            onChange={(e) => setFormData({ ...formData, lastName: e.target.value })} />
          <TextField fullWidth margin="normal" label="Email *" type="email" value={formData.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            disabled={!!editingCustomer} />
          <TextField fullWidth margin="normal" label="Phone Number" value={formData.phoneNumber}
            onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit}>
            {editingCustomer ? 'Update' : 'Create'}
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

export default CustomerList;
