import React from 'react';
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
} from '@mui/material';
import {
  People as PeopleIcon,
  Inventory as InventoryIcon,
  ShoppingCart as OrdersIcon,
  Receipt as InvoiceIcon,
} from '@mui/icons-material';

const StatCard = ({ title, value, icon, color }) => (
  <Card>
    <CardContent>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography color="textSecondary" gutterBottom>
            {title}
          </Typography>
          <Typography variant="h4">
            {value}
          </Typography>
        </Box>
        <Box sx={{ color, fontSize: 48 }}>
          {icon}
        </Box>
      </Box>
    </CardContent>
  </Card>
);

const Dashboard = () => {
  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>
      
      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Customers"
            value="0"
            icon={<PeopleIcon fontSize="inherit" />}
            color="#1976d2"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Products"
            value="0"
            icon={<InventoryIcon fontSize="inherit" />}
            color="#2e7d32"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Orders"
            value="0"
            icon={<OrdersIcon fontSize="inherit" />}
            color="#ed6c02"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Invoices"
            value="0"
            icon={<InvoiceIcon fontSize="inherit" />}
            color="#9c27b0"
          />
        </Grid>

        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Welcome to OrderFlow
            </Typography>
            <Typography variant="body1" color="textSecondary">
              Manage your customers, products, orders, and invoices all in one place.
            </Typography>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Dashboard;
