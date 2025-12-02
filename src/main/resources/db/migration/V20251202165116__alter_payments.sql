ALTER TABLE payments
ADD CONSTRAINT uk_payments_transaction UNIQUE (transaction_id);