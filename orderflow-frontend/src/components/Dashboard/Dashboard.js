import React, { useState, useEffect } from 'react';
import {
  Container, Grid, Paper, Typography, Box, Card, CardContent, CircularProgress,
} from '@mui/material';
import {
  People as PeopleIcon,
  Inventory as InventoryIcon,
  ShoppingCart as OrdersIcon,

  TrendingUp as TrendingUpIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
  Pending as PendingIcon,
} from '@mui/icons-material';
import { customerAPI, productAPI, orderAPI } from '../../services/api';

const StatCard = ({ title, value, icon, color, subtitle }) => (
  <Card sx={{ height: '100%', borderLeft: `4px solid ${color}` }}>
    <CardContent>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <Box>
          <Typography color="textSecondary" variant="body2" gutterBottom>
            {title}
          </Typography>
          <Typography variant="h3" fontWeight="bold" sx={{ color }}>
            {value}
          </Typography>
          {subtitle && (
            <Typography variant="body2" color="textSecondary" sx={{ mt: 0.5 }}>
              {subtitle}
            </Typography>
          )}
        </Box>
        <Box sx={{
          bgcolor: color + '20',
          borderRadius: 2,
          p: 1.5,
          color,
        }}>
          {icon}
        </Box>
      </Box>
    </CardContent>
  </Card>
);

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalCustomers: 0,
    activeCustomers: 0,
    totalProducts: 0,
    lowStockProducts: 0,
    totalOrders: 0,
    pendingOrders: 0,
    deliveredOrders: 0,
    cancelledOrders: 0,
    totalRevenue: 0,
  });

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      setLoading(true);
      const [customersRes, productsRes, ordersRes] = await Promise.all([
        customerAPI.getAll(),
        productAPI.getAll(),
        orderAPI.getAll(),
      ]);

      const customers = Array.isArray(customersRes.data) ? customersRes.data : [];
      const products = Array.isArray(productsRes.data) ? productsRes.data : [];
      const orders = Array.isArray(ordersRes.data) ? ordersRes.data : [];

      const activeCustomers = customers.filter(c => c.active && !c.deleted).length;
      const lowStock = products.filter(p => !p.deleted && p.quantityInStock <= 10).length;
      const pending = orders.filter(o => o.status === 'PENDING').length;
      const delivered = orders.filter(o => o.status === 'DELIVERED').length;
      const cancelled = orders.filter(o => o.status === 'CANCELLED').length;
      const revenue = orders
        .filter(o => o.status !== 'CANCELLED')
        .reduce((sum, o) => sum + (o.totalAmount || 0), 0);

      setStats({
        totalCustomers: customers.filter(c => !c.deleted).length,
        activeCustomers,
        totalProducts: products.filter(p => !p.deleted).length,
        lowStockProducts: lowStock,
        totalOrders: orders.length,
        pendingOrders: pending,
        deliveredOrders: delivered,
        cancelledOrders: cancelled,
        totalRevenue: revenue,
      });
    } catch (error) {
      console.error('Error fetching stats:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <CircularProgress size={60} />
      </Box>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" fontWeight="bold" gutterBottom>
          Dashboard
        </Typography>
        <Typography variant="body1" color="textSecondary">
          Welcome to OrderFlow! Here's your business overview.
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Row 1 - Main Stats */}
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Customers"
            value={stats.totalCustomers}
            icon={<PeopleIcon fontSize="large" />}
            color="#1976d2"
            subtitle={`${stats.activeCustomers} active`}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Products"
            value={stats.totalProducts}
            icon={<InventoryIcon fontSize="large" />}
            color="#2e7d32"
            subtitle={stats.lowStockProducts > 0 ? `⚠️ ${stats.lowStockProducts} low stock` : 'All stocked'}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Orders"
            value={stats.totalOrders}
            icon={<OrdersIcon fontSize="large" />}
            color="#ed6c02"
            subtitle={`${stats.pendingOrders} pending`}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Revenue"
            value={`₹${Number(stats.totalRevenue).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`}
            icon={<TrendingUpIcon fontSize="large" />}
            color="#9c27b0"
            subtitle="From all orders"
          />
        </Grid>

        {/* Row 2 - Order Status Breakdown */}
        <Grid item xs={12}>
          <Typography variant="h6" fontWeight="bold" sx={{ mt: 2, mb: 1 }}>
            Order Status Breakdown
          </Typography>
        </Grid>
        <Grid item xs={12} sm={4}>
          <StatCard
            title="Pending Orders"
            value={stats.pendingOrders}
            icon={<PendingIcon fontSize="large" />}
            color="#ed6c02"
            subtitle="Awaiting processing"
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <StatCard
            title="Delivered Orders"
            value={stats.deliveredOrders}
            icon={<CheckCircleIcon fontSize="large" />}
            color="#2e7d32"
            subtitle="Successfully completed"
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <StatCard
            title="Cancelled Orders"
            value={stats.cancelledOrders}
            icon={<CancelIcon fontSize="large" />}
            color="#d32f2f"
            subtitle="Cancelled by user"
          />
        </Grid>

        {/* Row 3 - Quick Summary */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3, mt: 2 }}>
            <Typography variant="h6" fontWeight="bold" gutterBottom>
              Quick Summary
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={6} sm={3}>
                <Box sx={{ textAlign: 'center', p: 2, bgcolor: '#e3f2fd', borderRadius: 2 }}>
                  <Typography variant="h4" color="primary" fontWeight="bold">
                    {stats.totalCustomers}
                  </Typography>
                  <Typography variant="body2">Customers</Typography>
                </Box>
              </Grid>
              <Grid item xs={6} sm={3}>
                <Box sx={{ textAlign: 'center', p: 2, bgcolor: '#e8f5e9', borderRadius: 2 }}>
                  <Typography variant="h4" color="success.main" fontWeight="bold">
                    {stats.totalProducts}
                  </Typography>
                  <Typography variant="body2">Products</Typography>
                </Box>
              </Grid>
              <Grid item xs={6} sm={3}>
                <Box sx={{ textAlign: 'center', p: 2, bgcolor: '#fff3e0', borderRadius: 2 }}>
                  <Typography variant="h4" color="warning.main" fontWeight="bold">
                    {stats.totalOrders}
                  </Typography>
                  <Typography variant="body2">Orders</Typography>
                </Box>
              </Grid>
              <Grid item xs={6} sm={3}>
                <Box sx={{ textAlign: 'center', p: 2, bgcolor: '#f3e5f5', borderRadius: 2 }}>
                  <Typography variant="h4" color="secondary.main" fontWeight="bold">
                    ₹{Number(stats.totalRevenue).toLocaleString('en-IN', { maximumFractionDigits: 0 })}
                  </Typography>
                  <Typography variant="body2">Revenue</Typography>
                </Box>
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Dashboard;
