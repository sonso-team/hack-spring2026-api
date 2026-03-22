CREATE UNIQUE INDEX ux_admins_single_superadmin ON admins (role) WHERE role = 'SUPERADMIN';
