-- src/main/resources/data.sql
DELETE FROM transactions;
DELETE FROM users;

-- Insert users with BCrypt encoded passwords (all passwords = "password123")
-- These hashes were generated with BCryptPasswordEncoder
INSERT INTO users (id, username, email, password, first_name, last_name, phone_number, status, created_at, updated_at) 
VALUES 
('user123', 'john_doe', 'john.doe@email.com', '$2a$12$hazw7lOWIPaZJYw44LVwPeV4kSx4GKMKvGPQVdVSy7yektWVy7amO', 'John', 'Doe', '+27123456789', 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('user456', 'jane_smith', 'jane.smith@email.com', '$2a$12$hazw7lOWIPaZJYw44LVwPeV4kSx4GKMKvGPQVdVSy7yektWVy7amO', 'Jane', 'Smith', '+27987654321', 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('user789', 'bob_wilson', 'bob.wilson@email.com', '$2a$12$hazw7lOWIPaZJYw44LVwPeV4kSx4GKMKvGPQVdVSy7yektWVy7amO', 'Bob', 'Wilson', '+27811223344', 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Insert transactions (as before)
INSERT INTO transactions (id, user_id, amount, currency, transaction_date, description, payment_method, status, category, created_at, updated_at) 
VALUES 
('11111111-1111-1111-1111-111111111111', 'user123', 1500.75, 'ZAR', '2024-01-15T10:30:00', 'Weekly grocery shopping at Checkers', 'DEBIT_CARD', 'COMPLETED', 'GROCERIES', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('22222222-2222-2222-2222-222222222222', 'user123', 450.50, 'ZAR', '2024-01-16T12:15:00', 'Lunch at Spur', 'CREDIT_CARD', 'COMPLETED', 'DINING', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('33333333-3333-3333-3333-333333333333', 'user123', 2999.99, 'ZAR', '2024-01-17T09:00:00', 'Takealot electronics purchase', 'CREDIT_CARD', 'COMPLETED', 'SHOPPING', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('44444444-4444-4444-4444-444444444444', 'user123', 850.20, 'ZAR', '2024-01-18T08:30:00', 'Uber trips in Cape Town', 'CREDIT_CARD', 'COMPLETED', 'TRANSPORTATION', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('55555555-5555-5555-5555-555555555555', 'user123', 12000.00, 'ZAR', '2024-01-19T14:00:00', 'Monthly rent payment', 'BANK_TRANSFER', 'COMPLETED', 'RENT', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('66666666-6666-6666-6666-666666666666', 'user123', 899.99, 'ZAR', '2024-01-20T19:30:00', 'Showmax and Spotify subscriptions', 'CREDIT_CARD', 'COMPLETED', 'ENTERTAINMENT', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('77777777-7777-7777-7777-777777777777', 'user123', 35000.00, 'ZAR', '2024-01-21T09:00:00', 'Salary deposit', 'BANK_TRANSFER', 'COMPLETED', 'INCOME', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

('88888888-8888-8888-8888-888888888888', 'user456', 25000.00, 'ZAR', '2024-01-15T14:00:00', 'Monthly salary - PnP', 'BANK_TRANSFER', 'COMPLETED', 'INCOME', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('99999999-9999-9999-9999-999999999999', 'user456', 1250.00, 'ZAR', '2024-01-16T11:20:00', 'Woolworths grocery', 'DEBIT_CARD', 'COMPLETED', 'GROCERIES', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'user456', 750.50, 'ZAR', '2024-01-17T13:45:00', 'Dinner at Ocean Basket', 'CREDIT_CARD', 'PENDING', 'DINING', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'user456', 1500.00, 'ZAR', '2024-01-18T10:15:00', 'Engen fuel', 'DEBIT_CARD', 'COMPLETED', 'TRANSPORTATION', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'user456', 450.99, 'ZAR', '2024-01-19T20:30:00', 'Ster-Kinekor movies', 'CREDIT_CARD', 'COMPLETED', 'ENTERTAINMENT', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'user456', 250.00, 'USD', '2024-01-20T09:00:00', 'Amazon.com purchase', 'CREDIT_CARD', 'COMPLETED', 'SHOPPING', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'user789', 15000.00, 'ZAR', '2024-01-15T09:00:00', 'Freelance payment', 'BANK_TRANSFER', 'COMPLETED', 'INCOME', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('ffffffff-ffff-ffff-ffff-ffffffffffff', 'user789', 89.90, 'EUR', '2024-01-16T16:30:00', 'Coursera subscription', 'CREDIT_CARD', 'COMPLETED', 'EDUCATION', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('gggggggg-gggg-gggg-gggg-gggggggggggg', 'user789', 3500.00, 'ZAR', '2024-01-17T12:00:00', 'Dischem pharmacy', 'DEBIT_CARD', 'COMPLETED', 'HEALTHCARE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('hhhhhhhh-hhhh-hhhh-hhhh-hhhhhhhhhhhh', 'user789', 120.50, 'GBP', '2024-01-18T14:30:00', 'Amazon UK order', 'CREDIT_CARD', 'COMPLETED', 'SHOPPING', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());