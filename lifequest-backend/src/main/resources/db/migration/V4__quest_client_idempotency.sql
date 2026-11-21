ALTER TABLE quests ADD COLUMN IF NOT EXISTS client_request_id VARCHAR(80);
CREATE UNIQUE INDEX IF NOT EXISTS uq_quest_client_request ON quests(user_id,client_request_id) WHERE client_request_id IS NOT NULL;
