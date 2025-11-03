-- Import dữ liệu phường/xã mẫu
-- File: V4__insert_communes_sample_data.sql

INSERT INTO Communes (CommuneCode, CommuneName, CommuneNameEn, CommuneFullName, CommuneFullNameEn, CodeName, ProvinceCode) VALUES
-- Các phường thuộc Hà Nội (mã tỉnh: 01)
-- Quận Ba Đình, Hà Nội
('00001', N'Phúc Xá', 'Phuc Xa', N'Phường Phúc Xá', 'Phuc Xa Ward', 'phuc_xa', '01'),
('00004', N'Trúc Bạch', 'Truc Bach', N'Phường Trúc Bạch', 'Truc Bach Ward', 'truc_bach', '01'),
('00006', N'Vĩnh Phúc', 'Vinh Phuc', N'Phường Vĩnh Phúc', 'Vinh Phuc Ward', 'vinh_phuc', '01'),
('00007', N'Cống Vị', 'Cong Vi', N'Phường Cống Vị', 'Cong Vi Ward', 'cong_vi', '01'),
('00008', N'Liễu Giai', 'Lieu Giai', N'Phường Liễu Giai', 'Lieu Giai Ward', 'lieu_giai', '01'),
('00010', N'Nguyễn Trung Trực', 'Nguyen Trung Truc', N'Phường Nguyễn Trung Trực', 'Nguyen Trung Truc Ward', 'nguyen_trung_truc', '01'),
('00013', N'Quán Thánh', 'Quan Thanh', N'Phường Quán Thánh', 'Quan Thanh Ward', 'quan_thanh', '01'),
('00016', N'Ngọc Hà', 'Ngoc Ha', N'Phường Ngọc Hà', 'Ngoc Ha Ward', 'ngoc_ha', '01'),
('00019', N'Điện Biên', 'Dien Bien', N'Phường Điện Biên', 'Dien Bien Ward', 'dien_bien', '01'),
('00022', N'Đội Cấn', 'Doi Can', N'Phường Đội Cấn', 'Doi Can Ward', 'doi_can', '01'),
('00025', N'Ngọc Khánh', 'Ngoc Khanh', N'Phường Ngọc Khánh', 'Ngoc Khanh Ward', 'ngoc_khanh', '01'),
('00028', N'Kim Mã', 'Kim Ma', N'Phường Kim Mã', 'Kim Ma Ward', 'kim_ma', '01'),
('00031', N'Giảng Võ', 'Giang Vo', N'Phường Giảng Võ', 'Giang Vo Ward', 'giang_vo', '01'),
('00034', N'Thành Công', 'Thanh Cong', N'Phường Thành Công', 'Thanh Cong Ward', 'thanh_cong', '01'),

-- Quận Hoàn Kiếm, Hà Nội
('00037', N'Phúc Tân', 'Phuc Tan', N'Phường Phúc Tân', 'Phuc Tan Ward', 'phuc_tan', '01'),
('00040', N'Đồng Xuân', 'Dong Xuan', N'Phường Đồng Xuân', 'Dong Xuan Ward', 'dong_xuan', '01'),
('00043', N'Hàng Mã', 'Hang Ma', N'Phường Hàng Mã', 'Hang Ma Ward', 'hang_ma', '01'),
('00046', N'Hàng Buồm', 'Hang Buom', N'Phường Hàng Buồm', 'Hang Buom Ward', 'hang_buom', '01'),
('00049', N'Hàng Đào', 'Hang Dao', N'Phường Hàng Đào', 'Hang Dao Ward', 'hang_dao', '01'),
('00052', N'Hàng Bồ', 'Hang Bo', N'Phường Hàng Bồ', 'Hang Bo Ward', 'hang_bo', '01'),
('00055', N'Cửa Đông', 'Cua Dong', N'Phường Cửa Đông', 'Cua Dong Ward', 'cua_dong', '01'),
('00058', N'Lý Thái Tổ', 'Ly Thai To', N'Phường Lý Thái Tổ', 'Ly Thai To Ward', 'ly_thai_to', '01'),
('00061', N'Hàng Bạc', 'Hang Bac', N'Phường Hàng Bạc', 'Hang Bac Ward', 'hang_bac', '01'),
('00064', N'Hàng Gai', 'Hang Gai', N'Phường Hàng Gai', 'Hang Gai Ward', 'hang_gai', '01'),
('00067', N'Chương Dương', 'Chuong Duong', N'Phường Chương Dương', 'Chuong Duong Ward', 'chuong_duong', '01'),
('00070', N'Hàng Trống', 'Hang Trong', N'Phường Hàng Trống', 'Hang Trong Ward', 'hang_trong', '01'),
('00073', N'Cửa Nam', 'Cua Nam', N'Phường Cửa Nam', 'Cua Nam Ward', 'cua_nam', '01'),
('00076', N'Hàng Bông', 'Hang Bong', N'Phường Hàng Bông', 'Hang Bong Ward', 'hang_bong', '01'),
('00079', N'Tràng Tiền', 'Trang Tien', N'Phường Tràng Tiền', 'Trang Tien Ward', 'trang_tien', '01'),
('00082', N'Trần Hưng Đạo', 'Tran Hung Dao', N'Phường Trần Hưng Đạo', 'Tran Hung Dao Ward', 'tran_hung_dao', '01'),
('00085', N'Phan Chu Trinh', 'Phan Chu Trinh', N'Phường Phan Chu Trinh', 'Phan Chu Trinh Ward', 'phan_chu_trinh', '01'),

-- Các phường thuộc TP.HCM (mã tỉnh: 79)
-- Quận 1, TP.HCM
('26734', N'Tân Định', 'Tan Dinh', N'Phường Tân Định', 'Tan Dinh Ward', 'tan_dinh', '79'),
('26737', N'Đa Kao', 'Da Kao', N'Phường Đa Kao', 'Da Kao Ward', 'da_kao', '79'),
('26740', N'Bến Nghé', 'Ben Nghe', N'Phường Bến Nghé', 'Ben Nghe Ward', 'ben_nghe', '79'),
('26743', N'Bến Thành', 'Ben Thanh', N'Phường Bến Thành', 'Ben Thanh Ward', 'ben_thanh', '79'),
('26746', N'Nguyễn Thái Bình', 'Nguyen Thai Binh', N'Phường Nguyễn Thái Bình', 'Nguyen Thai Binh Ward', 'nguyen_thai_binh', '79'),
('26749', N'Phạm Ngũ Lão', 'Pham Ngu Lao', N'Phường Phạm Ngũ Lão', 'Pham Ngu Lao Ward', 'pham_ngu_lao', '79'),
('26752', N'Cầu Ông Lãnh', 'Cau Ong Lanh', N'Phường Cầu Ông Lãnh', 'Cau Ong Lanh Ward', 'cau_ong_lanh', '79'),
('26755', N'Cô Giang', 'Co Giang', N'Phường Cô Giang', 'Co Giang Ward', 'co_giang', '79'),
('26758', N'Nguyễn Cư Trinh', 'Nguyen Cu Trinh', N'Phường Nguyễn Cư Trinh', 'Nguyen Cu Trinh Ward', 'nguyen_cu_trinh', '79'),
('26761', N'Cầu Kho', 'Cau Kho', N'Phường Cầu Kho', 'Cau Kho Ward', 'cau_kho', '79'),

-- Quận 3, TP.HCM
('26764', N'Võ Thị Sáu', 'Vo Thi Sau', N'Phường Võ Thị Sáu', 'Vo Thi Sau Ward', 'vo_thi_sau', '79'),
('26767', N'Phạm Đình Hổ', 'Pham Dinh Ho', N'Phường Phạm Đình Hổ', 'Pham Dinh Ho Ward', 'pham_dinh_ho', '79'),
('26770', N'Nguyễn Thị Minh Khai', 'Nguyen Thi Minh Khai', N'Phường Nguyễn Thị Minh Khai', 'Nguyen Thi Minh Khai Ward', 'nguyen_thi_minh_khai', '79'),
('26773', N'Nguyễn Đình Chiểu', 'Nguyen Dinh Chieu', N'Phường Nguyễn Đình Chiểu', 'Nguyen Dinh Chieu Ward', 'nguyen_dinh_chieu', '79'),
('26776', N'Lê Văn Sỹ', 'Le Van Sy', N'Phường Lê Văn Sỹ', 'Le Van Sy Ward', 'le_van_sy', '79'),
('26779', N'Nguyễn Cư Trinh', 'Nguyen Cu Trinh', N'Phường Nguyễn Cư Trinh', 'Nguyen Cu Trinh Ward', 'nguyen_cu_trinh_q3', '79'),

-- Thêm một số xã vùng ngoại thành
-- Huyện Sóc Sơn, Hà Nội
('00379', N'Sóc Sơn', 'Soc Son', N'Thị trấn Sóc Sơn', 'Soc Son Town', 'soc_son', '01'),
('00382', N'Bắc Sơn', 'Bac Son', N'Xã Bắc Sơn', 'Bac Son Commune', 'bac_son', '01'),
('00385', N'Minh Trí', 'Minh Tri', N'Xã Minh Trí', 'Minh Tri Commune', 'minh_tri', '01'),
('00388', N'Hồng Kỳ', 'Hong Ky', N'Xã Hồng Kỳ', 'Hong Ky Commune', 'hong_ky', '01'),
('00391', N'Nam Sơn', 'Nam Son', N'Xã Nam Sơn', 'Nam Son Commune', 'nam_son', '01'),

-- Huyện Củ Chi, TP.HCM
('26941', N'Củ Chi', 'Cu Chi', N'Thị trấn Củ Chi', 'Cu Chi Town', 'cu_chi', '79'),
('26944', N'Phú Mỹ Hưng', 'Phu My Hung', N'Xã Phú Mỹ Hưng', 'Phu My Hung Commune', 'phu_my_hung', '79'),
('26947', N'An Phú', 'An Phu', N'Xã An Phú', 'An Phu Commune', 'an_phu', '79'),
('26950', N'Trung Lập Thượng', 'Trung Lap Thuong', N'Xã Trung Lập Thượng', 'Trung Lap Thuong Commune', 'trung_lap_thuong', '79'),
('26953', N'An Nhơn Tây', 'An Nhon Tay', N'Xã An Nhơn Tây', 'An Nhon Tay Commune', 'an_nhon_tay', '79');