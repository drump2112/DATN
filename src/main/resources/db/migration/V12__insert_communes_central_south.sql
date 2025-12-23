-- Insert dữ liệu communes phần 2 - Miền Trung và Miền Nam
-- File: V12__insert_communes_central_south.sql

INSERT INTO Communes (CommuneCode, CommuneName, CommuneNameEn, CommuneFullName, CommuneFullNameEn, CodeName, ProvinceCode) VALUES

-- Thanh Hóa (ProvinceCode: 38)
('38001', N'Phường Ba Đình', 'Ba Dinh Ward', N'Phường Ba Đình', 'Ba Dinh Ward', 'ba_dinh_th', '38'),
('38004', N'Phường Lam Sơn', 'Lam Son Ward', N'Phường Lam Sơn', 'Lam Son Ward', 'lam_son', '38'),
('38007', N'Phường Hàm Rồng', 'Ham Rong Ward', N'Phường Hàm Rồng', 'Ham Rong Ward', 'ham_rong', '38'),
('38010', N'Phường Đông Vệ', 'Dong Ve Ward', N'Phường Đông Vệ', 'Dong Ve Ward', 'dong_ve', '38'),

-- Nghệ An (ProvinceCode: 40)
('40001', N'Phường Cửa Nam', 'Cua Nam Ward', N'Phường Cửa Nam', 'Cua Nam Ward', 'cua_nam', '40'),
('40004', N'Phường Quang Trung', 'Quang Trung Ward', N'Phường Quang Trung', 'Quang Trung Ward', 'quang_trung_na', '40'),
('40007', N'Phường Đông Vĩnh', 'Dong Vinh Ward', N'Phường Đông Vĩnh', 'Dong Vinh Ward', 'dong_vinh', '40'),
('40010', N'Phường Hà Huy Tập', 'Ha Huy Tap Ward', N'Phường Hà Huy Tập', 'Ha Huy Tap Ward', 'ha_huy_tap', '40'),

-- Hà Tĩnh (ProvinceCode: 42)
('42001', N'Phường Trần Phú', 'Tran Phu Ward', N'Phường Trần Phú', 'Tran Phu Ward', 'tran_phu_ht', '42'),
('42004', N'Phường Nam Hà', 'Nam Ha Ward', N'Phường Nam Hà', 'Nam Ha Ward', 'nam_ha', '42'),
('42007', N'Phường Bắc Hà', 'Bac Ha Ward', N'Phường Bắc Hà', 'Bac Ha Ward', 'bac_ha', '42'),
('42010', N'Phường Nguyễn Du', 'Nguyen Du Ward', N'Phường Nguyễn Du', 'Nguyen Du Ward', 'nguyen_du_ht', '42'),

-- Quảng Bình (ProvinceCode: 44)
('44001', N'Phường Đồng Phú', 'Dong Phu Ward', N'Phường Đồng Phú', 'Dong Phu Ward', 'dong_phu', '44'),
('44004', N'Phường Nam Lý', 'Nam Ly Ward', N'Phường Nam Lý', 'Nam Ly Ward', 'nam_ly', '44'),
('44007', N'Phường Đồng Hải', 'Dong Hai Ward', N'Phường Đồng Hải', 'Dong Hai Ward', 'dong_hai', '44'),
('44010', N'Phường Bắc Lý', 'Bac Ly Ward', N'Phường Bắc Lý', 'Bac Ly Ward', 'bac_ly', '44'),

-- Quảng Trị (ProvinceCode: 45)
('45001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_qt', '45'),
('45004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_qt', '45'),
('45007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_qt', '45'),
('45010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_qt', '45'),

-- Thừa Thiên Huế (ProvinceCode: 46)
('46001', N'Phường Phú Hòa', 'Phu Hoa Ward', N'Phường Phú Hòa', 'Phu Hoa Ward', 'phu_hoa', '46'),
('46004', N'Phường Thuận Thành', 'Thuan Thanh Ward', N'Phường Thuận Thành', 'Thuan Thanh Ward', 'thuan_thanh_tth', '46'),
('46007', N'Phường Thuận Lộc', 'Thuan Loc Ward', N'Phường Thuận Lộc', 'Thuan Loc Ward', 'thuan_loc', '46'),
('46010', N'Phường Phú Nhuận', 'Phu Nhuan Ward', N'Phường Phú Nhuận', 'Phu Nhuan Ward', 'phu_nhuan_hue', '46'),

-- Quảng Nam (ProvinceCode: 49)
('49001', N'Phường Tân Thạnh', 'Tan Thanh Ward', N'Phường Tân Thạnh', 'Tan Thanh Ward', 'tan_thanh_qn', '49'),
('49004', N'Phường Phước Hòa', 'Phuoc Hoa Ward', N'Phường Phước Hòa', 'Phuoc Hoa Ward', 'phuoc_hoa_qn', '49'),
('49007', N'Phường An Mỹ', 'An My Ward', N'Phường An Mỹ', 'An My Ward', 'an_my', '49'),
('49010', N'Phường Hòa Thuận', 'Hoa Thuan Ward', N'Phường Hòa Thuận', 'Hoa Thuan Ward', 'hoa_thuan_qn', '49'),

-- Quảng Ngãi (ProvinceCode: 51)
('51001', N'Phường Lý Thường Kiệt', 'Ly Thuong Kiet Ward', N'Phường Lý Thường Kiệt', 'Ly Thuong Kiet Ward', 'ly_thuong_kiet_qng', '51'),
('51004', N'Phường Trần Hưng Đạo', 'Tran Hung Dao Ward', N'Phường Trần Hưng Đạo', 'Tran Hung Dao Ward', 'tran_hung_dao_qng', '51'),
('51007', N'Phường Quang Trung', 'Quang Trung Ward', N'Phường Quang Trung', 'Quang Trung Ward', 'quang_trung_qng', '51'),
('51010', N'Phường Nghĩa Chánh', 'Nghia Chanh Ward', N'Phường Nghĩa Chánh', 'Nghia Chanh Ward', 'nghia_chanh', '51'),

-- Bình Định (ProvinceCode: 52)
('52001', N'Phường Lê Hồng Phong', 'Le Hong Phong Ward', N'Phường Lê Hồng Phong', 'Le Hong Phong Ward', 'le_hong_phong_bd', '52'),
('52004', N'Phường Lý Thường Kiệt', 'Ly Thuong Kiet Ward', N'Phường Lý Thường Kiệt', 'Ly Thuong Kiet Ward', 'ly_thuong_kiet_bd', '52'),
('52007', N'Phường Ghềnh Ráng', 'Ghenh Rang Ward', N'Phường Ghềnh Ráng', 'Ghenh Rang Ward', 'ghenh_rang', '52'),
('52010', N'Phường Trần Quang Diệu', 'Tran Quang Dieu Ward', N'Phường Trần Quang Diệu', 'Tran Quang Dieu Ward', 'tran_quang_dieu', '52'),

-- Phú Yên (ProvinceCode: 54)
('54001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_py', '54'),
('54004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_py', '54'),
('54007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_py', '54'),
('54010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_py', '54'),

-- Khánh Hòa (ProvinceCode: 56)
('56001', N'Phường Vĩnh Hải', 'Vinh Hai Ward', N'Phường Vĩnh Hải', 'Vinh Hai Ward', 'vinh_hai', '56'),
('56004', N'Phường Vĩnh Phước', 'Vinh Phuoc Ward', N'Phường Vĩnh Phước', 'Vinh Phuoc Ward', 'vinh_phuoc', '56'),
('56007', N'Phường Ngọc Hiệp', 'Ngoc Hiep Ward', N'Phường Ngọc Hiệp', 'Ngoc Hiep Ward', 'ngoc_hiep', '56'),
('56010', N'Phường Vĩnh Thọ', 'Vinh Tho Ward', N'Phường Vĩnh Thọ', 'Vinh Tho Ward', 'vinh_tho', '56'),
('56013', N'Phường Phước Long', 'Phuoc Long Ward', N'Phường Phước Long', 'Phuoc Long Ward', 'phuoc_long_kh', '56'),

-- Ninh Thuận (ProvinceCode: 58)
('58001', N'Phường Mỹ Bình', 'My Binh Ward', N'Phường Mỹ Bình', 'My Binh Ward', 'my_binh', '58'),
('58004', N'Phường Mỹ Đông', 'My Dong Ward', N'Phường Mỹ Đông', 'My Dong Ward', 'my_dong', '58'),
('58007', N'Phường Mỹ Hải', 'My Hai Ward', N'Phường Mỹ Hải', 'My Hai Ward', 'my_hai', '58'),
('58010', N'Phường Mỹ Hiệp', 'My Hiep Ward', N'Phường Mỹ Hiệp', 'My Hiep Ward', 'my_hiep', '58'),

-- Bình Thuận (ProvinceCode: 60)
('60001', N'Phường Mũi Né', 'Mui Ne Ward', N'Phường Mũi Né', 'Mui Ne Ward', 'mui_ne', '60'),
('60004', N'Phường Phú Hài', 'Phu Hai Ward', N'Phường Phú Hài', 'Phu Hai Ward', 'phu_hai', '60'),
('60007', N'Phường Phú Thủy', 'Phu Thuy Ward', N'Phường Phú Thủy', 'Phu Thuy Ward', 'phu_thuy', '60'),
('60010', N'Phường Thanh Hải', 'Thanh Hai Ward', N'Phường Thanh Hải', 'Thanh Hai Ward', 'thanh_hai', '60'),

-- Kon Tum (ProvinceCode: 62)
('62001', N'Phường Quang Trung', 'Quang Trung Ward', N'Phường Quang Trung', 'Quang Trung Ward', 'quang_trung_kt', '62'),
('62004', N'Phường Ba Đình', 'Ba Dinh Ward', N'Phường Ba Đình', 'Ba Dinh Ward', 'ba_dinh_kt', '62'),
('62007', N'Phường Trần Hưng Đạo', 'Tran Hung Dao Ward', N'Phường Trần Hưng Đạo', 'Tran Hung Dao Ward', 'tran_hung_dao_kt', '62'),
('62010', N'Phường Lê Lợi', 'Le Loi Ward', N'Phường Lê Lợi', 'Le Loi Ward', 'le_loi_kt', '62'),

-- Gia Lai (ProvinceCode: 64)
('64001', N'Phường Phù Đổng', 'Phu Dong Ward', N'Phường Phù Đổng', 'Phu Dong Ward', 'phu_dong', '64'),
('64004', N'Phường Diên Hồng', 'Dien Hong Ward', N'Phường Diên Hồng', 'Dien Hong Ward', 'dien_hong', '64'),
('64007', N'Phường Ia Kring', 'Ia Kring Ward', N'Phường Ia Kring', 'Ia Kring Ward', 'ia_kring', '64'),
('64010', N'Phường Tây Sơn', 'Tay Son Ward', N'Phường Tây Sơn', 'Tay Son Ward', 'tay_son_gl', '64'),

-- Đắk Lắk (ProvinceCode: 66)
('66001', N'Phường Tân Lợi', 'Tan Loi Ward', N'Phường Tân Lợi', 'Tan Loi Ward', 'tan_loi_dl', '66'),
('66004', N'Phường Thống Nhất', 'Thong Nhat Ward', N'Phường Thống Nhất', 'Thong Nhat Ward', 'thong_nhat_dl', '66'),
('66007', N'Phường Ea Tam', 'Ea Tam Ward', N'Phường Ea Tam', 'Ea Tam Ward', 'ea_tam', '66'),
('66010', N'Phường Khánh Xuân', 'Khanh Xuan Ward', N'Phường Khánh Xuân', 'Khanh Xuan Ward', 'khanh_xuan', '66'),

-- Đắk Nông (ProvinceCode: 67)
('67001', N'Phường Nghĩa Đức', 'Nghia Duc Ward', N'Phường Nghĩa Đức', 'Nghia Duc Ward', 'nghia_duc', '67'),
('67004', N'Phường Nghĩa Thành', 'Nghia Thanh Ward', N'Phường Nghĩa Thành', 'Nghia Thanh Ward', 'nghia_thanh', '67'),
('67007', N'Phường Nghĩa Phú', 'Nghia Phu Ward', N'Phường Nghĩa Phú', 'Nghia Phu Ward', 'nghia_phu', '67'),
('67010', N'Phường Nghĩa Tân', 'Nghia Tan Ward', N'Phường Nghĩa Tân', 'Nghia Tan Ward', 'nghia_tan', '67'),

-- Lâm Đồng (ProvinceCode: 68)
('68001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_ld', '68'),
('68004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_ld', '68'),
('68007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_ld', '68'),
('68010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_ld', '68'),
('68013', N'Phường 5', 'Ward 5', N'Phường 5', 'Ward 5', 'phuong_5_ld', '68'),

-- Bình Phước (ProvinceCode: 70)
('70001', N'Phường Tân Xuân', 'Tan Xuan Ward', N'Phường Tân Xuân', 'Tan Xuan Ward', 'tan_xuan_bp', '70'),
('70004', N'Phường Tân Phú', 'Tan Phu Ward', N'Phường Tân Phú', 'Tan Phu Ward', 'tan_phu_bp', '70'),
('70007', N'Phường Tân Đồng', 'Tan Dong Ward', N'Phường Tân Đồng', 'Tan Dong Ward', 'tan_dong', '70'),
('70010', N'Phường Tân Bình', 'Tan Binh Ward', N'Phường Tân Bình', 'Tan Binh Ward', 'tan_binh_bp', '70'),

-- Tây Ninh (ProvinceCode: 72)
('72001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_tn', '72'),
('72004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_tn', '72'),
('72007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_tn', '72'),
('72010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_tn', '72'),

-- Bình Dương (ProvinceCode: 74)
('74001', N'Phường Phú Lợi', 'Phu Loi Ward', N'Phường Phú Lợi', 'Phu Loi Ward', 'phu_loi', '74'),
('74004', N'Phường Phú Hòa', 'Phu Hoa Ward', N'Phường Phú Hòa', 'Phu Hoa Ward', 'phu_hoa_bd', '74'),
('74007', N'Phường Phú Thọ', 'Phu Tho Ward', N'Phường Phú Thọ', 'Phu Tho Ward', 'phu_tho_bd', '74'),
('74010', N'Phường Chánh Nghĩa', 'Chanh Nghia Ward', N'Phường Chánh Nghĩa', 'Chanh Nghia Ward', 'chanh_nghia', '74'),

-- Đồng Nai (ProvinceCode: 75)
('75001', N'Phường Quyết Thắng', 'Quyet Thang Ward', N'Phường Quyết Thắng', 'Quyet Thang Ward', 'quyet_thang_dn', '75'),
('75004', N'Phường Thống Nhất', 'Thong Nhat Ward', N'Phường Thống Nhất', 'Thong Nhat Ward', 'thong_nhat_dn', '75'),
('75007', N'Phường Dân Chủ', 'Dan Chu Ward', N'Phường Dân Chủ', 'Dan Chu Ward', 'dan_chu', '75'),
('75010', N'Phường Tân Tiến', 'Tan Tien Ward', N'Phường Tân Tiến', 'Tan Tien Ward', 'tan_tien', '75'),

-- Long An (ProvinceCode: 80)
('80001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_la', '80'),
('80004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_la', '80'),
('80007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_la', '80'),
('80010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_la', '80'),

-- Tiền Giang (ProvinceCode: 82)
('82001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_tg', '82'),
('82004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_tg', '82'),
('82007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_tg', '82'),
('82010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_tg', '82'),

-- Bến Tre (ProvinceCode: 83)
('83001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_bt', '83'),
('83004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_bt', '83'),
('83007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_bt', '83'),
('83010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_bt', '83'),

-- Trà Vinh (ProvinceCode: 84)
('84001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_tv', '84'),
('84004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_tv', '84'),
('84007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_tv', '84'),
('84010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_tv', '84'),

-- Vĩnh Long (ProvinceCode: 86)
('86001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_vl', '86'),
('86004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_vl', '86'),
('86007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_vl', '86'),
('86010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_vl', '86'),

-- Đồng Tháp (ProvinceCode: 87)
('87001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_dt', '87'),
('87004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_dt', '87'),
('87007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_dt', '87'),
('87010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_dt', '87'),

-- An Giang (ProvinceCode: 89)
('89001', N'Phường Mỹ Bình', 'My Binh Ward', N'Phường Mỹ Bình', 'My Binh Ward', 'my_binh_ag', '89'),
('89004', N'Phường Mỹ Long', 'My Long Ward', N'Phường Mỹ Long', 'My Long Ward', 'my_long', '89'),
('89007', N'Phường Mỹ Phước', 'My Phuoc Ward', N'Phường Mỹ Phước', 'My Phuoc Ward', 'my_phuoc', '89'),
('89010', N'Phường Mỹ Quý', 'My Quy Ward', N'Phường Mỹ Quý', 'My Quy Ward', 'my_quy', '89'),

-- Kiên Giang (ProvinceCode: 91)
('91001', N'Phường Vĩnh Thanh', 'Vinh Thanh Ward', N'Phường Vĩnh Thanh', 'Vinh Thanh Ward', 'vinh_thanh_kg', '91'),
('91004', N'Phường Vĩnh Lạc', 'Vinh Lac Ward', N'Phường Vĩnh Lạc', 'Vinh Lac Ward', 'vinh_lac', '91'),
('91007', N'Phường Vĩnh Hiệp', 'Vinh Hiep Ward', N'Phường Vĩnh Hiệp', 'Vinh Hiep Ward', 'vinh_hiep', '91'),
('91010', N'Phường An Hòa', 'An Hoa Ward', N'Phường An Hòa', 'An Hoa Ward', 'an_hoa_kg', '91'),

-- Hậu Giang (ProvinceCode: 93)
('93001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_hg', '93'),
('93004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_hg', '93'),
('93007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_hg', '93'),
('93010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_hg', '93'),

-- Sóc Trăng (ProvinceCode: 94)
('94001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_st', '94'),
('94004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_st', '94'),
('94007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_st', '94'),
('94010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_st', '94'),

-- Bạc Liêu (ProvinceCode: 95)
('95001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_bl', '95'),
('95004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_bl', '95'),
('95007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_bl', '95'),
('95010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_bl', '95'),

-- Cà Mau (ProvinceCode: 96)
('96001', N'Phường 1', 'Ward 1', N'Phường 1', 'Ward 1', 'phuong_1_cm', '96'),
('96004', N'Phường 2', 'Ward 2', N'Phường 2', 'Ward 2', 'phuong_2_cm', '96'),
('96007', N'Phường 3', 'Ward 3', N'Phường 3', 'Ward 3', 'phuong_3_cm', '96'),
('96010', N'Phường 4', 'Ward 4', N'Phường 4', 'Ward 4', 'phuong_4_cm', '96');