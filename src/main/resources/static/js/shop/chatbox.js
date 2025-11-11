$(document).ready(function() {
    console.log('Chatbox script loaded');

    const chatboxToggle = $('#chatbox-toggle');
    const chatboxContainer = $('#chatbox-container');
    const chatboxClose = $('#chatbox-close');
    const chatInput = $('#chat-input');
    const sendButton = $('#send-message');
    const chatBody = $('#chatbox-body');
    const chatNotification = $('#chat-notification');

    console.log('Chatbox elements found:', {
        toggle: chatboxToggle.length,
        container: chatboxContainer.length,
        close: chatboxClose.length,
        input: chatInput.length,
        send: sendButton.length,
        body: chatBody.length,
        notification: chatNotification.length
    });

    // Predefined responses
    const responses = {
        'xin chào': 'Xin chào! Rất vui được hỗ trợ bạn. Bạn cần tôi giúp gì?',
        'hello': 'Hello! Tôi có thể giúp gì cho bạn?',
        'sản phẩm': 'Chúng tôi có nhiều sản phẩm giày thể thao chất lượng từ các thương hiệu nổi tiếng như Nike, Adidas, Puma. Bạn quan tâm loại nào?',
        'giá': 'Giá sản phẩm của chúng tôi rất cạnh tranh, từ 500.000 VNĐ đến 3.000.000 VNĐ tùy theo từng dòng sản phẩm. Bạn có sản phẩm cụ thể nào muốn hỏi không?',
        'giao hàng': 'Chúng tôi hỗ trợ giao hàng toàn quốc trong 2-5 ngày làm việc. Phí giao hàng từ 20.000 VNĐ tùy theo khu vực.',
        'thanh toán': 'Chúng tôi hỗ trợ thanh toán COD (tiền mặt khi nhận hàng) và thanh toán online qua VNPay.',
        'size': 'Chúng tôi có đầy đủ size từ 36 đến 44. Bạn có thể tham khảo bảng size chi tiết ở trang sản phẩm.',
        'đổi trả': 'Chúng tôi hỗ trợ đổi trả trong vòng 7 ngày nếu sản phẩm còn nguyên tem mác và chưa sử dụng.',
        'liên hệ': 'Bạn có thể liên hệ với chúng tôi qua hotline: 0123-456-789 hoặc email: support@shop.com',
        'cảm ơn': 'Cảm ơn bạn! Rất vui được hỗ trợ. Chúc bạn mua sắm vui vẻ! 😊'
    };

    // Function to scroll chat to bottom
    function scrollToBottom() {
        chatBody.scrollTop(chatBody[0].scrollHeight);
    }

    // Toggle chatbox
    chatboxToggle.click(function() {
        console.log('Chatbox toggle clicked');
        if (chatboxContainer.is(':visible')) {
            console.log('Hiding chatbox');
            chatboxContainer.hide();
        } else {
            console.log('Showing chatbox');
            chatboxContainer.show();
            // Scroll to bottom immediately when opening
            setTimeout(function() {
                scrollToBottom();
                chatInput.focus();
            }, 50); // Small delay to ensure chatbox is fully rendered
            chatNotification.hide();
        }
    });

    // Close chatbox
    chatboxClose.click(function() {
        console.log('Chatbox close clicked');
        chatboxContainer.hide();
    });

    // Send message
    function sendMessage() {
        const message = chatInput.val().trim();
        console.log('Sending message:', message);
        if (message) {
            // Add user message
            addMessage(message, 'user');
            chatInput.val('');

            // Show typing indicator
            showTypingIndicator();

            // Auto-reply after delay
            setTimeout(function() {
                hideTypingIndicator();
                const response = getAutoReply(message);
                addMessage(response, 'admin');
            }, 1000 + Math.random() * 1000);
        }
    }

    // Add message to chat
    function addMessage(text, sender) {
        const isUser = sender === 'user';
        const messageClass = isUser ? 'user-message' : 'admin-message';
        const avatarIcon = isUser ? 'fa-user' : 'fa-user-circle';
        const time = getCurrentTime();

        const messageHtml = `
            <div class="message ${messageClass}">
                <div class="message-avatar">
                    <i class="fa ${avatarIcon}"></i>
                </div>
                <div class="message-content">
                    <p>${text}</p>
                    <span class="message-time">${time}</span>
                </div>
            </div>
        `;

        chatBody.append(messageHtml);
        chatBody.scrollTop(chatBody[0].scrollHeight);
    }

    // Get auto reply
    function getAutoReply(message) {
        const lowerMessage = message.toLowerCase();

        for (const keyword in responses) {
            if (lowerMessage.includes(keyword)) {
                return responses[keyword];
            }
        }

         const defaultResponses = [
            'Cảm ơn bạn đã nhắn tin! Tôi sẽ ghi nhận và phản hồi sớm nhất có thể.',
            'Câu hỏi của bạn rất thú vị! Để được hỗ trợ tốt nhất, bạn có thể liên hệ hotline: 0123-456-789',
            'Tôi hiểu câu hỏi của bạn. Nhân viên tư vấn sẽ liên hệ lại trong thời gian sớm nhất!',
            'Cảm ơn bạn quan tâm! Mọi thắc mắc xin liên hệ trực tiếp để được hỗ trợ chi tiết hơn.'
        ];

        return defaultResponses[Math.floor(Math.random() * defaultResponses.length)];
    }

    function showTypingIndicator() {
        const typingHtml = `
            <div class="message admin-message typing-indicator">
                <div class="message-avatar">
                    <i class="fa fa-user-circle"></i>
                </div>
                <div class="message-content">
                    <div class="typing-dots">
                        <span></span>
                        <span></span>
                        <span></span>
                    </div>
                </div>
            </div>
        `;
        chatBody.append(typingHtml);
        $('.typing-indicator').show();
        chatBody.scrollTop(chatBody[0].scrollHeight);
    }

    // Hide typing indicator
    function hideTypingIndicator() {
        $('.typing-indicator').remove();
    }

    // Get current time
    function getCurrentTime() {
        const now = new Date();
        return now.getHours().toString().padStart(2, '0') + ':' +
               now.getMinutes().toString().padStart(2, '0');
    }

    // Event listeners
    sendButton.click(sendMessage);

    chatInput.keypress(function(e) {
        if (e.which === 13) {
            sendMessage();
        }
    });

    // Show notification after some time (demo)
    setTimeout(function() {
        if (!chatboxContainer.is(':visible')) {
            chatNotification.show();
        }
    }, 10000);
});