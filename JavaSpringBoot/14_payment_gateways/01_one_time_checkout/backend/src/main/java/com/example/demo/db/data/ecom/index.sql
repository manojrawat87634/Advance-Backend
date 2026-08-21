INSERT INTO items (name, description, price, is_active, is_deleted) 
VALUES 
    (
        'Wireless Ergonomic Mouse', 
        'Multi-device bluetooth mouse with adjustable DPI and silent clicks.', 
        149900, -- ₹1,499.00
        TRUE, 
        FALSE
    ),
    (
        'Mechanical Gaming Keyboard', 
        'RGB backlit mechanical keyboard with tactile blue switches.', 
        349900, -- ₹3,499.00
        TRUE, 
        FALSE
    ),
    (
        'Noise Cancelling Headphones', 
        'Over-ear wireless headphones with active noise cancellation and 30-hour battery life.', 
        899900, -- ₹8,999.00
        TRUE, 
        FALSE
    ),
    (
        'USB-C Fast Charging Cable (2m)', 
        'Braided nylon USB-C to USB-C cable supporting 100W Power Delivery.', 
        49900,  -- ₹499.00
        TRUE, 
        FALSE
    ),
    (
        'Legacy USB-A Flash Drive 16GB', 
        'Discontinued older model USB 2.0 flash drive.', 
        29900,  -- ₹299.00
        FALSE,  -- Set to inactive
        FALSE
    ),
    (
        'Archived Prototype Stand', 
        'Experimental aluminum laptop stand design.', 
        120000, -- ₹1,200.00
        FALSE, 
        TRUE   -- Soft deleted item
    );