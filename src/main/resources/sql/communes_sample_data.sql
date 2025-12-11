-- Script import dữ liệu phường/xã (mẫu cho Hà Nội và TP.HCM)
-- Các phường thuộc Hà Nội (mã tỉnh: 01)

INSERT INTO Communes (CommuneCode, CommuneName, CommuneNameEn, CommuneFullName, CommuneFullNameEn, CodeName, ProvinceCode) VALUES
-- Quận Ba Đình, Hà Nội
('00001', 'Phúc Xá', 'Phuc Xa', 'Phường Phúc Xá', 'Phuc Xa Ward', 'phuc_xa', '01'),
('00004', 'Trúc Bạch', 'Truc Bach', 'Phường Trúc Bạch', 'Truc Bach Ward', 'truc_bach', '01'),
('00006', 'Vĩnh Phúc', 'Vinh Phuc', 'Phường Vĩnh Phúc', 'Vinh Phuc Ward', 'vinh_phuc', '01'),
('00007', 'Cống Vị', 'Cong Vi', 'Phường Cống Vị', 'Cong Vi Ward', 'cong_vi', '01'),
('00008', 'Liễu Giai', 'Lieu Giai', 'Phường Liễu Giai', 'Lieu Giai Ward', 'lieu_giai', '01'),
('00010', 'Nguyễn Trung Trực', 'Nguyen Trung Truc', 'Phường Nguyễn Trung Trực', 'Nguyen Trung Truc Ward', 'nguyen_trung_truc', '01'),
('00013', 'Quán Thánh', 'Quan Thanh', 'Phường Quán Thánh', 'Quan Thanh Ward', 'quan_thanh', '01'),
('00016', 'Ngọc Hà', 'Ngoc Ha', 'Phường Ngọc Hà', 'Ngoc Ha Ward', 'ngoc_ha', '01'),
('00019', 'Điện Biên', 'Dien Bien', 'Phường Điện Biên', 'Dien Bien Ward', 'dien_bien', '01'),
('00022', 'Đội Cấn', 'Doi Can', 'Phường Đội Cấn', 'Doi Can Ward', 'doi_can', '01'),
('00025', 'Ngọc Khánh', 'Ngoc Khanh', 'Phường Ngọc Khánh', 'Ngoc Khanh Ward', 'ngoc_khanh', '01'),
('00028', 'Kim Mã', 'Kim Ma', 'Phường Kim Mã', 'Kim Ma Ward', 'kim_ma', '01'),
('00031', 'Giảng Võ', 'Giang Vo', 'Phường Giảng Võ', 'Giang Vo Ward', 'giang_vo', '01'),
('00034', 'Thành Công', 'Thanh Cong', 'Phường Thành Công', 'Thanh Cong Ward', 'thanh_cong', '01'),

-- Quận Hoàn Kiếm, Hà Nội
('00037', 'Phúc Tân', 'Phuc Tan', 'Phường Phúc Tân', 'Phuc Tan Ward', 'phuc_tan', '01'),
('00040', 'Đồng Xuân', 'Dong Xuan', 'Phường Đồng Xuân', 'Dong Xuan Ward', 'dong_xuan', '01'),
('00043', 'Hàng Mã', 'Hang Ma', 'Phường Hàng Mã', 'Hang Ma Ward', 'hang_ma', '01'),
('00046', 'Hàng Buồm', 'Hang Buom', 'Phường Hàng Buồm', 'Hang Buom Ward', 'hang_buom', '01'),
('00049', 'Hàng Đào', 'Hang Dao', 'Phường Hàng Đào', 'Hang Dao Ward', 'hang_dao', '01'),
('00052', 'Hàng Bồ', 'Hang Bo', 'Phường Hàng Bồ', 'Hang Bo Ward', 'hang_bo', '01'),
('00055', 'Cửa Đông', 'Cua Dong', 'Phường Cửa Đông', 'Cua Dong Ward', 'cua_dong', '01'),
('00058', 'Lý Thái Tổ', 'Ly Thai To', 'Phường Lý Thái Tổ', 'Ly Thai To Ward', 'ly_thai_to', '01'),
('00061', 'Hàng Bạc', 'Hang Bac', 'Phường Hàng Bạc', 'Hang Bac Ward', 'hang_bac', '01'),
('00064', 'Hàng Gai', 'Hang Gai', 'Phường Hàng Gai', 'Hang Gai Ward', 'hang_gai', '01'),
('00067', 'Chương Dương', 'Chuong Duong', 'Phường Chương Dương', 'Chuong Duong Ward', 'chuong_duong', '01'),
('00070', 'Hàng Trống', 'Hang Trong', 'Phường Hàng Trống', 'Hang Trong Ward', 'hang_trong', '01'),
('00073', 'Cửa Nam', 'Cua Nam', 'Phường Cửa Nam', 'Cua Nam Ward', 'cua_nam', '01'),
('00076', 'Hàng Bông', 'Hang Bong', 'Phường Hàng Bông', 'Hang Bong Ward', 'hang_bong', '01'),
('00079', 'Tràng Tiền', 'Trang Tien', 'Phường Tràng Tiền', 'Trang Tien Ward', 'trang_tien', '01'),
('00082', 'Trần Hưng Đạo', 'Tran Hung Dao', 'Phường Trần Hưng Đạo', 'Tran Hung Dao Ward', 'tran_hung_dao', '01'),
('00085', 'Phan Chu Trinh', 'Phan Chu Trinh', 'Phường Phan Chu Trinh', 'Phan Chu Trinh Ward', 'phan_chu_trinh', '01'),

-- Các phường thuộc TP.HCM (mã tỉnh: 79)
-- Quận 1, TP.HCM
('26734', 'Tân Định', 'Tan Dinh', 'Phường Tân Định', 'Tan Dinh Ward', 'tan_dinh', '79'),
('26737', 'Đa Kao', 'Da Kao', 'Phường Đa Kao', 'Da Kao Ward', 'da_kao', '79'),
('26740', 'Bến Nghé', 'Ben Nghe', 'Phường Bến Nghé', 'Ben Nghe Ward', 'ben_nghe', '79'),
('26743', 'Bến Thành', 'Ben Thanh', 'Phường Bến Thành', 'Ben Thanh Ward', 'ben_thanh', '79'),
('26746', 'Nguyễn Thái Bình', 'Nguyen Thai Binh', 'Phường Nguyễn Thái Bình', 'Nguyen Thai Binh Ward', 'nguyen_thai_binh', '79'),
('26749', 'Phạm Ngũ Lão', 'Pham Ngu Lao', 'Phường Phạm Ngũ Lão', 'Pham Ngu Lao Ward', 'pham_ngu_lao', '79'),
('26752', 'Cầu Ông Lãnh', 'Cau Ong Lanh', 'Phường Cầu Ông Lãnh', 'Cau Ong Lanh Ward', 'cau_ong_lanh', '79'),
('26755', 'Cô Giang', 'Co Giang', 'Phường Cô Giang', 'Co Giang Ward', 'co_giang', '79'),
('26758', 'Nguyễn Cư Trinh', 'Nguyen Cu Trinh', 'Phường Nguyễn Cư Trinh', 'Nguyen Cu Trinh Ward', 'nguyen_cu_trinh', '79'),
('26761', 'Cầu Kho', 'Cau Kho', 'Phường Cầu Kho', 'Cau Kho Ward', 'cau_kho', '79'),

-- Quận 3, TP.HCM
('26764', 'Võ Thị Sáu', 'Vo Thi Sau', 'Phường Võ Thị Sáu', 'Vo Thi Sau Ward', 'vo_thi_sau', '79'),
('26767', 'Phạm Đình Hổ', 'Pham Dinh Ho', 'Phường Phạm Đình Hổ', 'Pham Dinh Ho Ward', 'pham_dinh_ho', '79'),
('26770', 'Nguyễn Thị Minh Khai', 'Nguyen Thi Minh Khai', 'Phường Nguyễn Thị Minh Khai', 'Nguyen Thi Minh Khai Ward', 'nguyen_thi_minh_khai', '79'),
('26773', 'Nguyễn Đình Chiểu', 'Nguyen Dinh Chieu', 'Phường Nguyễn Đình Chiểu', 'Nguyen Dinh Chieu Ward', 'nguyen_dinh_chieu', '79'),
('26776', 'Lê Văn Sỹ', 'Le Van Sy', 'Phường Lê Văn Sỹ', 'Le Van Sy Ward', 'le_van_sy', '79'),
('26779', 'Nguyễn Cư Trinh', 'Nguyen Cu Trinh', 'Phường Nguyễn Cư Trinh', 'Nguyen Cu Trinh Ward', 'nguyen_cu_trinh_q3', '79'),

-- Thêm một số xã vùng ngoại thành
-- Huyện Sóc Sơn, Hà Nội
('00379', 'Sóc Sơn', 'Soc Son', 'Thị trấn Sóc Sơn', 'Soc Son Town', 'soc_son', '01'),
('00382', 'Bắc Sơn', 'Bac Son', 'Xã Bắc Sơn', 'Bac Son Commune', 'bac_son', '01'),
('00385', 'Minh Trí', 'Minh Tri', 'Xã Minh Trí', 'Minh Tri Commune', 'minh_tri', '01'),
('00388', 'Hồng Kỳ', 'Hong Ky', 'Xã Hồng Kỳ', 'Hong Ky Commune', 'hong_ky', '01'),
('00391', 'Nam Sơn', 'Nam Son', 'Xã Nam Sơn', 'Nam Son Commune', 'nam_son', '01'),

-- Huyện Củ Chi, TP.HCM
('26941', 'Củ Chi', 'Cu Chi', 'Thị trấn Củ Chi', 'Cu Chi Town', 'cu_chi', '79'),
('26944', 'Phú Mỹ Hưng', 'Phu My Hung', 'Xã Phú Mỹ Hưng', 'Phu My Hung Commune', 'phu_my_hung', '79'),
('26947', 'An Phú', 'An Phu', 'Xã An Phú', 'An Phu Commune', 'an_phu', '79'),
('26950', 'Trung Lập Thượng', 'Trung Lap Thuong', 'Xã Trung Lập Thượng', 'Trung Lap Thuong Commune', 'trung_lap_thuong', '79'),
('26953', 'An Nhơn Tây', 'An Nhon Tay', 'Xã An Nhơn Tây', 'An Nhon Tay Commune', 'an_nhon_tay', '79');