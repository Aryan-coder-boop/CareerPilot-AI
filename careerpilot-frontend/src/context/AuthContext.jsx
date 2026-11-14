import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('cp_user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('cp_access_token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('cp_access_token');
      if (storedToken) {
        try {
          const profileResponse = await authService.getCurrentUser();
          if (profileResponse.success && profileResponse.data) {
            setUser(profileResponse.data);
            localStorage.setItem('cp_user', JSON.stringify(profileResponse.data));
          }
        } catch (error) {
          console.warn('Initial session check failed, logging out');
          authService.logout();
          setUser(null);
          setToken(null);
        }
      }
      setLoading(false);
    };

    const handleExternalLogout = () => {
      setUser(null);
      setToken(null);
    };

    window.addEventListener('auth-logout', handleExternalLogout);
    initAuth();

    return () => {
      window.removeEventListener('auth-logout', handleExternalLogout);
    };
  }, []);

  const login = async ({ email, password }) => {
    const response = await authService.login({ email, password });
    if (response.success && response.data) {
      const { accessToken, refreshToken, user: authUser } = response.data;
      localStorage.setItem('cp_access_token', accessToken);
      localStorage.setItem('cp_refresh_token', refreshToken);
      localStorage.setItem('cp_user', JSON.stringify(authUser));
      setToken(accessToken);
      setUser(authUser);
      return response;
    }
    throw new Error(response.message || 'Authentication failed');
  };

  const register = async ({ fullName, email, password }) => {
    const response = await authService.register({ fullName, email, password });
    if (response.success && response.data) {
      const { accessToken, refreshToken, user: authUser } = response.data;
      localStorage.setItem('cp_access_token', accessToken);
      localStorage.setItem('cp_refresh_token', refreshToken);
      localStorage.setItem('cp_user', JSON.stringify(authUser));
      setToken(accessToken);
      setUser(authUser);
      return response;
    }
    throw new Error(response.message || 'Registration failed');
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setToken(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        loading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

