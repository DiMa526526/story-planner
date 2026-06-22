/**
 * Главный компонент приложения
 * Настройка роутинга и провайдеров
 */
import { HashRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { ThemeProvider } from './contexts/ThemeContext';
import Layout from './components/common/Layout';
import ProtectedRoute from './components/common/ProtectedRoute';

// Страницы
import Landing from './pages/Landing';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import ForgotPassword from './pages/auth/ForgotPassword';
import ResetPassword from './pages/auth/ResetPassword';
import Dashboard from './pages/stories/Dashboard';
import StoryForm from './pages/stories/StoryForm';
import StoryDetail from './pages/stories/StoryDetail';
import Characters from './pages/characters/Characters';
import CharacterForm from './pages/characters/CharacterForm';
import Events from './pages/events/Events';
import EventForm from './pages/events/EventForm';
import EventDetail from './pages/events/EventDetail';
import EventLinkForm from './pages/events/EventLinkForm';
import Relationships from './pages/relationships/Relationships';
import RelationshipHistory from './pages/relationships/RelationshipHistory';
import Timeline from './pages/timeline/Timeline';

function AppRoutes() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        
        <Route path="/dashboard" element={
          <ProtectedRoute><Dashboard /></ProtectedRoute>
        } />
        
        <Route path="/stories/new" element={
          <ProtectedRoute><StoryForm /></ProtectedRoute>
        } />
        <Route path="/stories/:id/edit" element={
          <ProtectedRoute><StoryForm /></ProtectedRoute>
        } />
        <Route path="/stories/:id" element={
          <ProtectedRoute><StoryDetail /></ProtectedRoute>
        } />
        
        <Route path="/stories/:storyId/characters" element={
          <ProtectedRoute><Characters /></ProtectedRoute>
        } />
        <Route path="/stories/:storyId/characters/new" element={
          <ProtectedRoute><CharacterForm /></ProtectedRoute>
        } />
        <Route path="/characters/:characterId" element={
          <ProtectedRoute><CharacterForm /></ProtectedRoute>
        } />
        <Route path="/characters/:characterId/edit" element={
          <ProtectedRoute><CharacterForm /></ProtectedRoute>
        } />
        
        <Route path="/stories/:storyId/events" element={
          <ProtectedRoute><Events /></ProtectedRoute>
        } />
        <Route path="/stories/:storyId/events/new" element={
          <ProtectedRoute><EventForm /></ProtectedRoute>
        } />
        <Route path="/stories/:storyId/events/:eventId" element={
          <ProtectedRoute><EventDetail /></ProtectedRoute>
        } />
        <Route path="/events/:eventId" element={
          <ProtectedRoute><EventDetail /></ProtectedRoute>
        } />
        <Route path="/events/:eventId/edit" element={
          <ProtectedRoute><EventForm /></ProtectedRoute>
        } />
        
        <Route path="/stories/:storyId/events/:eventId/links/new" element={
          <ProtectedRoute><EventLinkForm /></ProtectedRoute>
        } />
        
        <Route path="/stories/:storyId/relationships" element={
          <ProtectedRoute><Relationships /></ProtectedRoute>
        } />
        {/* ИСПРАВЛЕНО: добавлен storyId в путь */}
        <Route path="/stories/:storyId/relationships/:relationshipId/history" element={
          <ProtectedRoute><RelationshipHistory /></ProtectedRoute>
        } />
        
        <Route path="/stories/:storyId/timeline" element={
          <ProtectedRoute><Timeline /></ProtectedRoute>
        } />
      </Routes>
    </Layout>
  );
}

function App() {
  return (
    <HashRouter>
      <ThemeProvider>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </ThemeProvider>
    </HashRouter>
  );
}

export default App;