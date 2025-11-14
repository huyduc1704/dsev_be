-- 1) Remove enum-typed default that blocks dropping the type
ALTER TABLE public.orders
    ALTER COLUMN status DROP DEFAULT;

-- 2) Convert the column to VARCHAR
ALTER TABLE public.orders
    ALTER COLUMN status TYPE VARCHAR(20)
        USING status::text;

-- 3) Normalize existing values to match Java enum (optional if you renamed Java)
UPDATE public.orders
SET status = 'CANCELED'
WHERE status = 'CANCELLED';

-- 4) Recreate default as plain text (optional)
ALTER TABLE public.orders
    ALTER COLUMN status SET DEFAULT 'PENDING';

-- 5) Drop the enum type (will succeed now)
DROP TYPE IF EXISTS order_status;