-- ==========================================
-- Moto-Parking Supabase Database Schema
-- ==========================================

-- ==========================================
-- 1. CHECK-INS TABLE (for 打卡 feature)
-- ==========================================
CREATE TABLE check_ins (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  spot_id UUID NOT NULL REFERENCES parking_spots(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_check_ins_spot_id ON check_ins(spot_id);

ALTER TABLE check_ins ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can view check_ins" ON check_ins
  FOR SELECT USING (true);

CREATE POLICY "Users can insert own check_ins" ON check_ins
  FOR INSERT WITH CHECK (auth.uid() = user_id);

-- RPC function for counting check-ins
CREATE OR REPLACE FUNCTION get_spot_check_in_count(p_spot_id UUID)
RETURNS INTEGER AS $$
  SELECT COALESCE(COUNT(*)::INTEGER, 0) FROM check_ins WHERE spot_id = p_spot_id;
$$ LANGUAGE SQL STABLE;

-- RPC function to check if user can check in (24-hour cooldown)
CREATE OR REPLACE FUNCTION can_user_check_in(p_user_id UUID, p_spot_id UUID)
RETURNS BOOLEAN AS $$
  SELECT NOT EXISTS(
    SELECT 1 FROM check_ins
    WHERE user_id = p_user_id
    AND spot_id = p_spot_id
    AND created_at > NOW() - INTERVAL '24 hours'
  );
$$ LANGUAGE SQL STABLE;


-- ==========================================
-- 2. SPOT REPORTS TABLE (for 回報問題 feature)
-- ==========================================
CREATE TABLE spot_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  spot_id UUID NOT NULL REFERENCES parking_spots(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  category TEXT NOT NULL,
  comment TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE spot_reports ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can insert own reports" ON spot_reports
  FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can view own reports" ON spot_reports
  FOR SELECT USING (auth.uid() = user_id);


-- ==========================================
-- 3. USER FAVORITES TABLE (for 收藏 feature)
-- ==========================================
CREATE TABLE IF NOT EXISTS user_favorites (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  spot_id UUID NOT NULL REFERENCES parking_spots(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(user_id, spot_id)
);

ALTER TABLE user_favorites ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own favorites" ON user_favorites
  FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own favorites" ON user_favorites
  FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own favorites" ON user_favorites
  FOR DELETE USING (auth.uid() = user_id);
