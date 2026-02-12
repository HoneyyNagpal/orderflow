import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Alert,
  MenuItem,
} from '@mui/material';
import { useAuth } from '../../contexts/AuthContext';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    role: 'CUSTOMER',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    const result = await register(formData);
    if (result.success) {
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } else {
      // Handle error properly - convert object to string
      const errorMessage = typeof result.error === 'string' 
        ? result.error 
        : result.error?.message || 'Registration failed';
      setError(errorMessage);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 8, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Typography variant="h4" align="center" gutterBottom>
            OrderFlow
          </Typography>
          <Typography variant="h6" align="center" color="textSecondary" gutterBottom>
            Create Account
          </Typography>

          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          {success && <Alert severity="success" sx={{ mb: 2 }}>Registration successful! Redirecting to login...</Alert>}

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Username"
              name="username"
              margin="normal"
              value={formData.username}
              onChange={handleChange}
              required
            />
            <TextField
              fullWidth
              label="Email"
              name="email"
              type="email"
              margin="normal"
              value={formData.email}
              onChange={handleChange}
              required
            />
            <TextField
              fullWidth
              label="First Name"
              name="firstName"
              margin="normal"
              value={formData.firstName}
              onChange={handleChange}
              required
            />
            <TextField
              fullWidth
              label="Last Name"
              name="lastName"
              margin="normal"
              value={formData.lastName}
              onChange={handleChange}
              required
            />
            <TextField
              fullWidth
              label="Password"
              name="password"
              type="password"
              margin="normal"
              value={formData.password}
              onChange={handleChange}
              required
            />
            <TextField
              fullWidth
              select
              label="Role"
              name="role"
              margin="normal"
              value={formData.role}
              onChange={handleChange}
            >
              <MenuItem value="CUSTOMER">Customer</MenuItem>
              <MenuItem value="ADMIN">Admin</MenuItem>
              <MenuItem value="MANAGER">Manager</MenuItem>
            </TextField>
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
            >
              Sign Up
            </Button>
            <Box sx={{ textAlign: 'center' }}>
              <Link to="/login" style={{ textDecoration: 'none' }}>
                Already have an account? Sign In
              </Link>
            </Box>
          </form>
        </Paper>
      </Box>
    </Container>
  );
};

export default Register;
