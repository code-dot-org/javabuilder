require 'minitest/autorun'
require_relative '../session_id_helper'
include SessionIdHelper

class SessionIdHelperTest < Minitest::Test
  def test_strips_single_trailing_padding_character
    assert_equal 'PoofdetIoAMCJpg', get_session_id(event_with_connection_id('PoofdetIoAMCJpg='))
  end

  def test_strips_multiple_trailing_padding_characters
    assert_equal 'gQwo_Q8CiQAYKAImwA', get_session_id(event_with_connection_id('gQwo_Q8CiQAYKAImwA=='))
  end

  def test_leaves_connection_id_without_padding_unchanged
    assert_equal 'abc-123_XYZ', get_session_id(event_with_connection_id('abc-123_XYZ'))
  end

  private

  def event_with_connection_id(connection_id)
    {"requestContext" => {"connectionId" => connection_id}}
  end
end
