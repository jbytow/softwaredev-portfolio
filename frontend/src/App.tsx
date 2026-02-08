import { Routes, Route } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import Layout from './components/Layout';
import AdminLayout from './admin/AdminLayout';
import ProtectedRoute from './components/ProtectedRoute';
import LoadingSpinner from './components/LoadingSpinner';
import ErrorBoundary from './components/ErrorBoundary';

// Public pages
const Home = lazy(() => import('./pages/Home'));
const About = lazy(() => import('./pages/About'));
const Experience = lazy(() => import('./pages/Experience'));
const Projects = lazy(() => import('./pages/Projects'));
const PostDetail = lazy(() => import('./pages/PostDetail'));
const Skills = lazy(() => import('./pages/Skills'));
const Contact = lazy(() => import('./pages/Contact'));

// Admin pages
const AdminLogin = lazy(() => import('./admin/AdminLogin'));
const AdminDashboard = lazy(() => import('./admin/AdminDashboard'));
const AdminPosts = lazy(() => import('./admin/AdminPosts'));
const AdminPostForm = lazy(() => import('./admin/AdminPostForm'));
const AdminSoftSkills = lazy(() => import('./admin/AdminSoftSkills'));
const AdminSoftSkillForm = lazy(() => import('./admin/AdminSoftSkillForm'));
const AdminSkillCategories = lazy(() => import('./admin/AdminSkillCategories'));
const AdminSkillCategoryForm = lazy(() => import('./admin/AdminSkillCategoryForm'));
const AdminExperiences = lazy(() => import('./admin/AdminExperiences'));
const AdminExperienceForm = lazy(() => import('./admin/AdminExperienceForm'));
const AdminInterests = lazy(() => import('./admin/AdminInterests'));
const AdminInterestForm = lazy(() => import('./admin/AdminInterestForm'));
const AdminAchievements = lazy(() => import('./admin/AdminAchievements'));
const AdminAchievementForm = lazy(() => import('./admin/AdminAchievementForm'));
const AdminRpgStats = lazy(() => import('./admin/AdminRpgStats'));
const AdminRpgStatForm = lazy(() => import('./admin/AdminRpgStatForm'));
const AdminMedia = lazy(() => import('./admin/AdminMedia'));
const AdminSettings = lazy(() => import('./admin/AdminSettings'));

function App() {
  return (
    <ErrorBoundary>
      <Suspense fallback={<LoadingSpinner fullScreen />}>
        <Routes>
        {/* Public Routes */}
        <Route path="/" element={<Layout />}>
          <Route index element={<Home />} />
          <Route path="about" element={<About />} />
          <Route path="experience" element={<Experience />} />
          <Route path="projects" element={<Projects />} />
          <Route path="projects/:slug" element={<PostDetail />} />
          <Route path="skills" element={<Skills />} />
          <Route path="contact" element={<Contact />} />
        </Route>

        {/* Admin Routes */}
        <Route path="/admin/login" element={<AdminLogin />} />
        <Route
          path="/admin"
          element={
            <ProtectedRoute>
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<AdminDashboard />} />
          <Route path="posts" element={<AdminPosts />} />
          <Route path="posts/new" element={<AdminPostForm />} />
          <Route path="posts/:id/edit" element={<AdminPostForm />} />
          <Route path="experiences" element={<AdminExperiences />} />
          <Route path="experiences/new" element={<AdminExperienceForm />} />
          <Route path="experiences/:id/edit" element={<AdminExperienceForm />} />
          <Route path="soft-skills" element={<AdminSoftSkills />} />
          <Route path="soft-skills/new" element={<AdminSoftSkillForm />} />
          <Route path="soft-skills/:id/edit" element={<AdminSoftSkillForm />} />
          <Route path="skill-categories" element={<AdminSkillCategories />} />
          <Route path="skill-categories/new" element={<AdminSkillCategoryForm />} />
          <Route path="skill-categories/:id/edit" element={<AdminSkillCategoryForm />} />
          <Route path="interests" element={<AdminInterests />} />
          <Route path="interests/new" element={<AdminInterestForm />} />
          <Route path="interests/:id/edit" element={<AdminInterestForm />} />
          <Route path="achievements" element={<AdminAchievements />} />
          <Route path="achievements/new" element={<AdminAchievementForm />} />
          <Route path="achievements/:id/edit" element={<AdminAchievementForm />} />
          <Route path="rpg-stats" element={<AdminRpgStats />} />
          <Route path="rpg-stats/new" element={<AdminRpgStatForm />} />
          <Route path="rpg-stats/:id/edit" element={<AdminRpgStatForm />} />
          <Route path="media" element={<AdminMedia />} />
          <Route path="settings" element={<AdminSettings />} />
        </Route>
        </Routes>
      </Suspense>
    </ErrorBoundary>
  );
}

export default App;
