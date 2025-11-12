-- =====================================================
-- Flyway Migration V2: Seed Data
-- Archivo: src/main/resources/db/migration/V2__seed_data.sql
-- =====================================================

-- =====================================================
-- CONFIGURACIÓN DEL SISTEMA
-- =====================================================
INSERT INTO system_configuration (config_key, config_value, data_type, description, is_sensitive) VALUES
('tokenization.rejection.probability', '0.10', 'DECIMAL', 'Probabilidad de rechazo en tokenización (0.0 - 1.0)', false),
('payment.rejection.probability', '0.15', 'DECIMAL', 'Probabilidad de rechazo en pagos (0.0 - 1.0)', false),
('payment.max.retry.attempts', '3', 'INTEGER', 'Número máximo de reintentos de pago', false),
('product.min.stock.visibility', '5', 'INTEGER', 'Stock mínimo para mostrar productos', false),
('cart.expiration.hours', '72', 'INTEGER', 'Horas antes de que expire un carrito', false),
('email.max.retries', '3', 'INTEGER', 'Número máximo de reintentos de envío de email', false),
('api.rate.limit.requests', '100', 'INTEGER', 'Límite de requests por minuto por API key', false),
('system.maintenance.mode', 'false', 'BOOLEAN', 'Modo de mantenimiento del sistema', false),
('order.number.prefix', 'ORD', 'STRING', 'Prefijo para números de orden', false),
('tax.rate.percentage', '16.00', 'DECIMAL', 'Porcentaje de IVA (México)', false);

-- =====================================================
-- PRODUCTOS DE PRUEBA
-- =====================================================

-- Medicamentos
INSERT INTO products (sku, name, description, price, stock, category, brand, is_active) VALUES
('MED-001', 'Paracetamol 500mg', 'Analgésico y antipirético - Caja con 20 tabletas', 45.50, 150, 'MEDICAMENTOS', 'GenericPharma', true),
('MED-002', 'Ibuprofeno 400mg', 'Antiinflamatorio no esteroideo - Caja con 30 tabletas', 89.00, 200, 'MEDICAMENTOS', 'PharmaPlus', true),
('MED-003', 'Amoxicilina 500mg', 'Antibiótico de amplio espectro - Caja con 21 cápsulas', 125.00, 80, 'MEDICAMENTOS', 'GenericPharma', true),
('MED-004', 'Loratadina 10mg', 'Antihistamínico para alergias - Caja con 10 tabletas', 55.00, 120, 'MEDICAMENTOS', 'AllergyRelief', true),
('MED-005', 'Omeprazol 20mg', 'Inhibidor de bomba de protones - Caja con 14 cápsulas', 95.00, 100, 'MEDICAMENTOS', 'DigestPlus', true),
('MED-006', 'Losartán 50mg', 'Antihipertensivo - Caja con 30 tabletas', 145.00, 90, 'MEDICAMENTOS', 'CardioHealth', true),
('MED-007', 'Metformina 850mg', 'Antidiabético oral - Caja con 30 tabletas', 78.00, 110, 'MEDICAMENTOS', 'DiabetCare', true),
('MED-008', 'Aspirina 100mg', 'Antiagregante plaquetario - Caja con 30 tabletas', 42.00, 180, 'MEDICAMENTOS', 'GenericPharma', true);

-- Vitaminas y Suplementos
INSERT INTO products (sku, name, description, price, stock, category, brand, is_active) VALUES
('VIT-001', 'Vitamina C 1000mg', 'Suplemento vitamínico - Frasco con 60 cápsulas', 120.00, 85, 'VITAMINAS', 'HealthPlus', true),
('VIT-002', 'Vitamina D3 5000 UI', 'Suplemento de vitamina D - Frasco con 90 cápsulas', 185.00, 70, 'VITAMINAS', 'VitalLife', true),
('VIT-003', 'Complejo B', 'Vitaminas del complejo B - Frasco con 100 tabletas', 145.00, 95, 'VITAMINAS', 'HealthPlus', true),
('VIT-004', 'Omega 3 1000mg', 'Ácidos grasos esenciales - Frasco con 60 cápsulas', 235.00, 60, 'VITAMINAS', 'OceanHealth', true),
('VIT-005', 'Multivitamínico Premium', 'Fórmula completa de vitaminas y minerales - Frasco 90 tabs', 199.00, 75, 'VITAMINAS', 'VitalLife', true),
('VIT-006', 'Calcio + Vitamina D', 'Suplemento para huesos - Frasco con 60 tabletas', 155.00, 88, 'VITAMINAS', 'BoneStrong', true),
('VIT-007', 'Magnesio 500mg', 'Suplemento mineral - Frasco con 90 cápsulas', 168.00, 72, 'VITAMINAS', 'HealthPlus', true),
('VIT-008', 'Zinc 50mg', 'Suplemento para sistema inmune - Frasco con 100 tabletas', 142.00, 65, 'VITAMINAS', 'ImmuneBoost', true);

-- Higiene y Cuidado Personal
INSERT INTO products (sku, name, description, price, stock, category, brand, is_active) VALUES
('HIG-001', 'Gel Antibacterial 500ml', 'Gel antibacterial con 70% alcohol', 65.00, 300, 'HIGIENE', 'CleanCare', true),
('HIG-002', 'Jabón Líquido Antibacterial 1L', 'Jabón líquido para manos', 85.00, 250, 'HIGIENE', 'CleanCare', true),
('HIG-003', 'Toallas Húmedas Antibacteriales', 'Paquete con 50 toallas húmedas', 45.00, 200, 'HIGIENE', 'FreshClean', true),
('HIG-004', 'Alcohol en Gel 250ml', 'Gel desinfectante portátil', 38.00, 280, 'HIGIENE', 'CleanCare', true),
('HIG-005', 'Desinfectante Spray 400ml', 'Spray desinfectante multiusos', 95.00, 150, 'HIGIENE', 'HomeClean', true),
('HIG-006', 'Shampoo Anticaspa 400ml', 'Shampoo medicado para caspa', 125.00, 95, 'HIGIENE', 'HairCare', true),
('HIG-007', 'Pasta Dental Blanqueadora 150g', 'Pasta dental con flúor', 68.00, 180, 'HIGIENE', 'SmileBright', true),
('HIG-008', 'Enjuague Bucal 500ml', 'Enjuague antiséptico bucal', 82.00, 140, 'HIGIENE', 'OralHealth', true),
('HIG-009', 'Desodorante Roll-On 50ml', 'Desodorante antitranspirante', 55.00, 220, 'HIGIENE', 'FreshDay', true),
('HIG-010', 'Bloqueador Solar FPS 50+ 120ml', 'Protección solar de amplio espectro', 185.00, 110, 'HIGIENE', 'SunProtect', true);

-- Equipos y Dispositivos Médicos
INSERT INTO products (sku, name, description, price, stock, category, brand, is_active) VALUES
('EQP-001', 'Termómetro Digital', 'Termómetro digital de lectura rápida', 199.00, 45, 'EQUIPOS', 'MedTech', true),
('EQP-002', 'Oxímetro de Pulso', 'Medidor de saturación de oxígeno', 485.00, 35, 'EQUIPOS', 'MedTech', true),
('EQP-003', 'Baumanómetro Digital', 'Monitor de presión arterial digital', 650.00, 28, 'EQUIPOS', 'CardioMonitor', true),
('EQP-004', 'Glucómetro + 25 Tiras', 'Medidor de glucosa en sangre', 395.00, 42, 'EQUIPOS', 'DiabetCheck', true),
('EQP-005', 'Nebulizador Portátil', 'Nebulizador para terapia respiratoria', 875.00, 22, 'EQUIPOS', 'RespirCare', true),
('EQP-006', 'Báscula Digital', 'Báscula con medición de grasa corporal', 545.00, 38, 'EQUIPOS', 'HealthScale', true),
('EQP-007', 'Tensiómetro Aneroide', 'Tensiómetro manual profesional', 420.00, 30, 'EQUIPOS', 'MedPro', true),
('EQP-008', 'Estetoscopio Profesional', 'Estetoscopio de doble campana', 680.00, 25, 'EQUIPOS', 'MedPro', true);

-- Primeros Auxilios
INSERT INTO products (sku, name, description, price, stock, category, brand, is_active) VALUES
('AUX-001', 'Botiquín Básico', 'Kit de primeros auxilios con 45 piezas', 285.00, 65, 'PRIMEROS_AUXILIOS', 'SafetyFirst', true),
('AUX-002', 'Vendas Elásticas 10cm x 5m', 'Vendas de compresión - Paquete con 3', 95.00, 120, 'PRIMEROS_AUXILIOS', 'MedSupply', true),
('AUX-003', 'Gasas Estériles 10x10cm', 'Caja con 25 sobres estériles', 85.00, 150, 'PRIMEROS_AUXILIOS', 'MedSupply', true),
('AUX-004', 'Alcohol Antiséptico 250ml', 'Alcohol isopropílico al 70%', 42.00, 200, 'PRIMEROS_AUXILIOS', 'CleanCare', true),
('AUX-005', 'Agua Oxigenada 120ml', 'Peróxido de hidrógeno al 3%', 28.00, 180, 'PRIMEROS_AUXILIOS', 'CleanCare', true),
('AUX-006', 'Curitas Surtidas', 'Caja con 100 curitas de diferentes tamaños', 68.00, 140, 'PRIMEROS_AUXILIOS', 'SafetyFirst', true),
('AUX-007', 'Micropore 2.5cm x 9m', 'Cinta adhesiva hipoalergénica', 45.00, 160, 'PRIMEROS_AUXILIOS', 'MedSupply', true),
('AUX-008', 'Guantes Látex Talla M', 'Caja con 100 guantes desechables', 145.00, 95, 'PRIMEROS_AUXILIOS', 'SafeGuard', true);

-- Cuidado Respiratorio
INSERT INTO products (sku, name, description, price, stock, category, brand, is_active) VALUES
('RES-001', 'Cubrebocas KN95', 'Caja con 20 cubrebocas de alta filtración', 185.00, 220, 'RESPIRATORIO', 'SafeBreathe', true),
('RES-002', 'Cubrebocas Quirúrgico Tricapa', 'Caja con 50 cubrebocas desechables', 125.00, 300, 'RESPIRATORIO', 'MedProtect', true),
('RES-003', 'Inhalador para Asma', 'Dispositivo espaciador para inhalador', 165.00, 55, 'RESPIRATORIO', 'RespirCare', true),
('RES-004', 'Vaporizador Ultrasónico', 'Humidificador de aire ultrasónico', 495.00, 40, 'RESPIRATORIO', 'AirComfort', true);

-- Suplementos Deportivos
INSERT INTO products (sku, name, description, price, stock, category, brand, is_active) VALUES
('DEP-001', 'Proteína Whey 1kg', 'Proteína de suero sabor chocolate', 685.00, 48, 'DEPORTIVOS', 'MuscleGain', true),
('DEP-002', 'Creatina Monohidrato 300g', 'Suplemento para rendimiento deportivo', 385.00, 52, 'DEPORTIVOS', 'PowerSport', true),
('DEP-003', 'BCAA 2:1:1 200g', 'Aminoácidos ramificados', 425.00, 45, 'DEPORTIVOS', 'MuscleGain', true),
('DEP-004', 'Pre-Workout Energía 300g', 'Suplemento pre-entreno', 465.00, 38, 'DEPORTIVOS', 'PowerSport', true),
('DEP-005', 'Glutamina 300g', 'L-Glutamina pura en polvo', 345.00, 42, 'DEPORTIVOS', 'RecoverPlus', true),
('DEP-006', 'Barras Proteicas Pack 12', 'Barras de proteína sabores variados', 285.00, 75, 'DEPORTIVOS', 'SnackFit', true);

-- Bebés y Maternidad
INSERT INTO products (sku, name, description, price, stock, category, brand, is_active) VALUES
('BEB-001', 'Pañales Recién Nacido x36', 'Pañales ultra absorbentes talla RN', 165.00, 120, 'BEBES', 'BabyComfort', true),
('BEB-002', 'Toallitas Húmedas Bebé x80', 'Toallitas húmedas sin alcohol', 58.00, 200, 'BEBES', 'BabySoft', true),
('BEB-003', 'Crema Antipañalitis 100g', 'Crema protectora con óxido de zinc', 95.00, 140, 'BEBES', 'BabyCare', true),
('BEB-004', 'Shampoo Bebé 400ml', 'Shampoo sin lágrimas pH balanceado', 85.00, 110, 'BEBES', 'BabySoft', true),
('BEB-005', 'Biberón Anticólico 240ml', 'Biberón con sistema anticólico', 185.00, 65, 'BEBES', 'BabyFeed', true),
('BEB-006', 'Termómetro Infrarrojo Bebé', 'Termómetro digital sin contacto', 385.00, 35, 'BEBES', 'BabyHealth', true);

-- Dermatología
INSERT INTO products (sku, name, description, price, stock, category, brand, is_active) VALUES
('DER-001', 'Crema Hidratante Facial 50ml', 'Crema facial hipoalergénica', 185.00, 85, 'DERMATOLOGIA', 'SkinCare', true),
('DER-002', 'Protector Labial FPS 30', 'Bálsamo labial con protección solar', 48.00, 150, 'DERMATOLOGIA', 'LipProtect', true),
('DER-003', 'Gel Limpiador Facial 200ml', 'Gel de limpieza para todo tipo de piel', 145.00, 95, 'DERMATOLOGIA', 'CleanSkin', true),
('DER-004', 'Serum Vitamina C 30ml', 'Serum facial antioxidante', 385.00, 55, 'DERMATOLOGIA', 'GlowSkin', true),
('DER-005', 'Crema Anti-Acné 30g', 'Tratamiento tópico para acné', 165.00, 72, 'DERMATOLOGIA', 'ClearSkin', true),
('DER-006', 'Exfoliante Corporal 250ml', 'Exfoliante con microesferas naturales', 125.00, 68, 'DERMATOLOGIA', 'SkinRenewal', true);
