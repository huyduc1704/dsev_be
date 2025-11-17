
-- 1\) Bỏ default (nếu có) để không bị chặn khi đổi kiểu
ALTER TABLE public.payments
    ALTER COLUMN payment_status DROP DEFAULT;

-- 2\) Đổi kiểu cột từ enum sang VARCHAR
ALTER TABLE public.payments
    ALTER COLUMN payment_status TYPE VARCHAR(20)
    USING payment_status::text;

-- 3\) (Tuỳ chọn) Chuẩn hoá giá trị nếu trước đây khác với enum Java
-- UPDATE public.payments
-- SET payment_status = 'FAILED'
-- WHERE payment_status = 'FAIL';

-- 4\) Đặt lại default dưới dạng text (tuỳ chọn)
ALTER TABLE public.payments
    ALTER COLUMN payment_status SET DEFAULT 'PENDING';

-- 5\) Xoá enum type PostgreSQL (nếu không còn cột nào dùng)
DROP TYPE IF EXISTS payment_status;