import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { AuthProvider } from './contexts/AuthContext';
import PrivateRoute from './components/Auth/PrivateRoute';
import Login from './components/Auth/Login';
import Register from './components/Auth/Register';
import Layout from './components/Layout/Layout';
import Dashboard from './components/Dashboard/Dashboard';
import CustomerList from './components/Customers/CustomerList';
import ProductList from './components/Products/ProductList';
import OrderList from './components/Orders/OrderList';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
              path="/"
              element={
                <PrivateRoute>
                  <Layout />
                </PrivateRoute>
              }
            >
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="customers" element={<CustomerList />} />
              <Route path="products" element={<ProductList />} />
              <Route path="orders" element={<OrderList />} />
            </Route>
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
